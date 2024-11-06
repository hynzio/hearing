package org.hdmd.hearingdemo.service;

import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.DeviceManageDTO;
import org.hdmd.hearingdemo.dto.DeviceMonitorDTO;
import org.hdmd.hearingdemo.exception.DeviceNotFoundException;
import org.hdmd.hearingdemo.model.Device;
import org.hdmd.hearingdemo.repository.DeviceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final NotificationService notificationService;

    //토큰 등록
//    public void registerToken(UserDTO userDTO){
//        Optional<User> existingUser =  userRepository.findById(userDTO.getId());
//        if (existingUser.isPresent()) {
//            // 이미 등록된 디바이스가 있다면 FCM 토큰 업데이트
//            User user = existingUser.get();
//            user.setFcmToken(userDTO.getFcmToken());
//            user.setLastUpdated(LocalDateTime.now());
//            userRepository.save(user);
//    }
//    }

    //단말기 등록
    @Transactional
    public Device registerDevice(DeviceManageDTO deviceManageDTO) {
        try {
            Device device = new Device();
            device.setDeviceName(deviceManageDTO.getDeviceName());
            device.setDeviceNum(deviceManageDTO.getDeviceNum());
            device.setAddress(deviceManageDTO.getAddress());
            return deviceRepository.save(device);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("제약 조건을 위반한 데이터가 있습니다: " + e.getMessage(), e);
        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //단말기 정보 조회
    public DeviceMonitorDTO getDeviceInfo(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("단말기를 찾을 수 없습니다."));

        return new DeviceMonitorDTO(device.getId(), device.getDeviceName(), device.getAddress());
    }

    //단말기 정보수정
    public void updateDeviceInfo(Long id, DeviceManageDTO deviceManageDTO) {
        try {
            Device device = deviceRepository.findById(id)
                    .orElseThrow(() -> new DeviceNotFoundException("단말기를 찾을 수 없습니다."));
            device.setDeviceName(deviceManageDTO.getDeviceName());
            device.setAddress(deviceManageDTO.getAddress());
            deviceRepository.save(device);
        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //단말기 상태 업데이트
    public void updateDeviceStatus(Long id, Boolean newStatus) {
        try {
            Device device = deviceRepository.findById(id)
                    .orElseThrow(() -> new DeviceNotFoundException("단말기를 찾을 수 없습니다."));
            device.setStatus(newStatus);
            deviceRepository.save(device);
            //notificationService.sendExitOrReturnNotification(newStatus);

        } catch (TransactionSystemException e) {
            throw new RuntimeException("트랜잭션 처리 중 오류가 발생했습니다: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("서버 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 단말기 삭제
//    public void deleteDevice(Long id) {
//        if (!deviceRepository.existsById(id)) {
//            throw new DeviceNotFoundException("단말기를 찾을 수 없습니다.");
//        }
//        deviceRepository.deleteById(id);
//
//    }

}