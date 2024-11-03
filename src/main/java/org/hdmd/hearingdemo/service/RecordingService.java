package org.hdmd.hearingdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awssdk.services.s3.S3Client;
import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.*;
import org.hdmd.hearingdemo.exception.DeviceNotFoundException;
import org.hdmd.hearingdemo.exception.RecordingNotFoundException;
import org.hdmd.hearingdemo.model.Device;
import org.hdmd.hearingdemo.model.History;
import org.hdmd.hearingdemo.model.Recording;
import org.hdmd.hearingdemo.repository.DeviceRepository;
import org.hdmd.hearingdemo.repository.HistoryRepository;
import org.hdmd.hearingdemo.repository.RecordingRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import software.amazon.awssdk.services.s3.model.*;

import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordingService {
    private final ClovaSpeechClient clovaSpeechClient;
    private final RecordingRepository recordingRepository;
    private final DeviceRepository deviceRepository;
    private final HistoryRepository historyRepository;
    private final AIService aiService;
    private final S3Client s3Client;
    private final NotificationService notificationService;

//    @Value("${clova.secretKey}")
//    String secretKey;
//
//    @Value("${clova.invokeUrl}")
//    String invokeUrl;

    @Value("${ai.api.url}")
    String aiUrl;

    public Recording processRecording(RecordingDTO recordingDTO) throws Exception {
        // 받은 데이터 DB에 저장
        RecordingDownloadDTO downloadDTO = uploadRecording(recordingDTO);
        File downloadedFile = downloadRecording(downloadDTO.getFilepath());
            updateRecordingStatus(downloadDTO.getRecordingId(), "처리 중");
        Map<String, Object> sttResult = clovaSpeechClient.soundToText(downloadedFile);
        List<String> analyzeResult = aiService.analyzeTextForDanger(sttResult);
        updateRecordingStatus(downloadDTO.getRecordingId(), "AI 검토 완료");


        // 4. 위험 알림 전송 (필요 시)

        Recording recording = recordingRepository.findById(downloadDTO.getRecordingId()).orElse(null);
        if (recording == null) {
            recording = new Recording();
            // 새로운 Recording 객체 초기화 (새로운 녹음 데이터 저장)
            recording.setDevice(deviceRepository.findById(recording.getDevice().getId()).orElseThrow(() -> new RuntimeException("Device not found")));
            recording.setFilepath(recordingDTO.getFilepath());
            recording.setTimestamp(recordingDTO.getTimestamp());
            recording.setLatitude(recordingDTO.getLatitude());
            recording.setLongitude(recordingDTO.getLongitude());
            recording.setStatus("검토 대기"); // 기본 상태 설정
        }

        if (!analyzeResult.isEmpty()) {
            updateRecordingReview(downloadDTO.getRecordingId(), true);
            recording.setText(analyzeResult);
            recordingRepository.save(recording);
            // 위험 알림 전송
            notificationService.sendDangerNotification(downloadDTO.getRecordingId());

        } else {
            // 위험하지 않은 경우 Recording.aiReview: false로 저장
            updateRecordingReview(downloadDTO.getRecordingId(), false);
            updateRecordingStatus(downloadDTO.getRecordingId(), "위험상황 종료");
        }

        // 5. 임시 파일 삭제
        deleteTempFile(downloadedFile);
        return recording;
    }

    //추출된 메타데이터 엔티티로 저장
    public RecordingDownloadDTO uploadRecording(RecordingDTO uploadDTO) {
        RecordingDownloadDTO downloadDTO;
        try {
            // Recording 엔티티에 메타데이터 저장
            Device device = deviceRepository.findById(uploadDTO.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found"));

            Recording recording = new Recording();
            recording.setDevice(device);
            recording.setFilepath(uploadDTO.getFilepath());
            recording.setTimestamp(uploadDTO.getTimestamp());
            recording.setLatitude(uploadDTO.getLatitude());
            recording.setLongitude(uploadDTO.getLongitude());
            recording.setStatus("검토 대기");  // 기본 상태 설정

            // DB에 저장
            recordingRepository.save(recording);

            downloadDTO = new RecordingDownloadDTO();
            downloadDTO.setRecordingId(recording.getId());
            downloadDTO.setFilepath(recording.getFilepath());

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("제약조건을 위반한 데이터가 있습니다: " + e.getMessage(), e);

        } catch (TransactionSystemException e) {
            throw new TransactionSystemException("저장 과정에서 문제가 발생했습니다.");
        } catch (DataAccessException e) {
            throw new RuntimeException("데이터베이스 접근 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("잘못된 데이터가 포함되어 있습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("데이터 저장 중 서버 오류가 발생했습니다.");
        }

        return downloadDTO;
    }

    public File downloadRecording(String filepath){
        String fileName = "recording_" + System.currentTimeMillis() + ".mp3"; // 파일 이름 생성
        String downloadDir = "C:/Users/jh377/Downloads";
        File downloadedFile = new File(downloadDir, fileName);

        String bucketName = extractBucketName(filepath);
        String objectKey = extractObjectKey(filepath);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.getObject(getObjectRequest, downloadedFile.toPath());
        return downloadedFile;
    }

    // 버킷 이름 추출 메서드
    private String extractBucketName(String s3Url) {
        String[] parts = s3Url.split("\\.");
        return parts[0].substring("https://".length());
    }

    // 객체 키 추출 메서드
    private String extractObjectKey(String s3Url) {
        return s3Url.substring(s3Url.indexOf(".com/") + 5);
    }

    //로컬파일 삭제
    private void deleteTempFile(File tempfile) {
        String localFilePath = tempfile.getPath();
        if (tempfile.exists()) {
            if (tempfile.delete()) {
                System.out.println(localFilePath + "에 저장된파일이 성공적으로 삭제되었습니다.");
            } else {
                System.out.println(localFilePath + "에 저장된 파일을 삭제하지 못했습니다.");
            }
        }
    }

    //위험상황 데이터 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getRecordingInfo(Long recordingId) throws JsonProcessingException {

        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RecordingNotFoundException("녹음을 찾을 수 없습니다."));

        Device device = recording.getDevice();
        if (device == null) {
            throw new DeviceNotFoundException("단말기를 찾을 수 없습니다.");
        }

        RecordingInfoDTO recordingInfoDTO = RecordingInfoDTO.builder()
                .recordingId(recording.getId())
                .timestamp(recording.getTimestamp())
                .latitude(recording.getLatitude())
                .longitude(recording.getLongitude())
                .filepath(recording.getFilepath())
                .text(recording.getTextAsList())
                .build();

        DeviceMonitorDTO deviceMonitorDTO = DeviceMonitorDTO.builder()
                .deviceId(device.getId())
                .deviceName(device.getDeviceName())
                .address(device.getAddress())
                .build();


        Map<String, Object> response = new HashMap<>();
        response.put("recording", recordingInfoDTO);
        response.put("device", deviceMonitorDTO);
        return response;
    }


    //기록으로 저장
    @Transactional
    public void moveToHistory(RecordingSendDTO recordingSendDTO) {
        try {
            Recording recording = recordingRepository.findById(recordingSendDTO.getRecordingId())
                    .orElseThrow(() -> new RecordingNotFoundException("녹음을 찾을 수 없습니다."));
            updateRecordingStatus(recordingSendDTO.getRecordingId(), "사용자 검토 완료");
            updateRecordingReview(recordingSendDTO.getRecordingId(), true);

            History history = new History();
            history.setDevice(recording.getDevice());
            history.setTimestamp(recording.getTimestamp());
            history.setText(String.valueOf(recording.getText()));
            history.setFilepath(recording.getFilepath());
            history.setLocation(recordingSendDTO.getLocation());

            historyRepository.save(history);
            ResponseEntity.ok("실제 위험으로 확인되어 기록으로 저장했습니다.");
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("제약조건을 위반한 데이터가 있습니다." + e.getMessage(), e);

        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //녹음삭제(실제로 위험하지 않은 데이터)
    public void notDanger(Long id) {
        if (!recordingRepository.existsById(id)) {
            throw new RecordingNotFoundException(("삭제할 녹음을 찾을 수 없습니다."));
        }
        updateRecordingReview(id, false);
        ResponseEntity.ok("녹음이 성공적으로 삭제되었습니다."); //사실 삭제 안됨
    }


    //녹음파일 상태 업데이트
    public void updateRecordingStatus(Long recordingId, String newStatus) {
        try {
            Recording recording = recordingRepository.findById(recordingId)
                    .orElseThrow(() -> new RecordingNotFoundException("상태를 업데이트할 녹음이 없습니다."));
            recording.setStatus(newStatus);
            recordingRepository.save(recording);

        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("데이터 상태 업데이트 중 서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    //
    public void updateRecordingReview(Long recordingId, boolean isDangerous) {
        try {
            Recording recording = recordingRepository.findById(recordingId)
                    .orElseThrow(() -> new RecordingNotFoundException("상태를 업데이트할 녹음이 없습니다."));
            if (recording.getStatus().equals("AI 검토 완료")){
                recording.setAiReview(isDangerous);
            }else if (recording.getStatus().equals("사용자 검토 완료")) {
                recording.setUserReview(isDangerous);
            }
            recordingRepository.save(recording);

        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("데이터 상태 업데이트 중 서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

//    //녹음파일 S3에서 삭제
//    private void deleteRecordingFromS3(String filePath) {
//        try {
//            // S3 클라이언트 사용하여 파일 삭제
//            s3Client.deleteObject(DeleteObjectRequest.builder()
//                    .bucket("handmadeai")  // S3 버킷 이름
//                    .key(filePath)  // 삭제할 파일 경로
//                    .build());
//        } catch (S3Exception e) {
//            throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
//        }
//
//    }
}