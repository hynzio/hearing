package org.hdmd.hearingdemo.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.RecordingDTO;
import org.hdmd.hearingdemo.dto.RecordingSendDTO;
import org.hdmd.hearingdemo.model.Recording;
import org.hdmd.hearingdemo.service.RecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.sql.SQLException;
import java.util.Map;

@Tag(name = "위험상황 데이터 확인", description = "위험상황 데이터(녹음파일) 관련  API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api1/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @PostMapping("/recording")
    @Operation(
            summary = "녹음파일 업로드",
            description = "단말기에서 S3로 녹음파일 업로드",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 업로드됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 파일"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public Recording processRecording(@RequestBody RecordingDTO uploadDTO) throws Exception {
        Recording recording = recordingService.processRecording(uploadDTO);
        return ResponseEntity.ok(recording).getBody();
    }


    //특정 위험상황 조회
    @GetMapping("/recording/{recordingId}")
    @Operation(
            summary="위험상황데이터 조회",
            description = "위험상황 데이터를 조회하는 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    @Parameter(name = "recordingId", description = "해당하는 녹음파일 id로 데이터 조회")
    public ResponseEntity<Map<String, Object>>  getRecordingInfo(@PathVariable Long recordingId) throws JsonProcessingException {
        Map<String, Object> response = recordingService.getRecordingInfo(recordingId);
        return ResponseEntity.ok(response);

    }

    //보호자 판단 및 기록으로 저장
    @PutMapping("/toHistory")
    @Operation(
            summary = "기록으로 저장",
            description = "해당 데이터를 기록으로 저장",
            responses = {
                @ApiResponse(responseCode = "200", description = "성공적으로 저장됨"),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<?> userReview(
            @RequestBody RecordingSendDTO recordingSendDTO) throws SQLException {
        recordingService.moveToHistory(recordingSendDTO);
        return ResponseEntity.ok("위험상황이 종료되었습니다.");

    }

    @PutMapping("/recording/{recordingId}")
    @Operation(
            summary = "사용자 검토 결과: 위험 미감지",
            description = "사용자가 위험으로 판단하지 않은 데이터 분류",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 삭제됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<String> deleteRecording(@PathVariable Long recordingId) {
            recordingService.notDanger(recordingId);
            return ResponseEntity.ok("위험하지 않은 데이터로 분류되었습니다.");
    }
}