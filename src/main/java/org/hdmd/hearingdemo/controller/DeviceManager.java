package org.hdmd.hearingdemo.controller;

import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.DeviceManageDTO;
import org.hdmd.hearingdemo.dto.DeviceMonitorDTO;
import org.hdmd.hearingdemo.model.Device;
import org.hdmd.hearingdemo.service.DeviceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*")
@Tag(name = "기기관리", description = "기기 정보 관리 API")
@RestController
@RequestMapping("/api1/devices")
@RequiredArgsConstructor
public class DeviceManager {

    private final DeviceService deviceService;
//    private final LocationService locationService;


    @PostMapping("/device")
    @Operation(summary = "기기등록", description = "신규기기 등록기능(사용X")
    public ResponseEntity<Device> registerDevice(@RequestBody DeviceManageDTO deviceManageDTO) {
        Device savedDevice = deviceService.registerDevice(deviceManageDTO);
        return ResponseEntity.status(201).body(savedDevice);
    }

    @Operation(summary = "단말기 관리", description = "해당하는 단말기 정보를 수정 후 저장함")
    @PutMapping("device/{deviceId}")
    public ResponseEntity<Void> updateDeviceInfo(@PathVariable Long deviceId, @RequestBody DeviceManageDTO deviceManageDTO) {
        deviceService.updateDeviceInfo(deviceId, deviceManageDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device/{id}")
    @Operation(summary = "기기 조회")
    public ResponseEntity<DeviceMonitorDTO> getDeviceInfo(@PathVariable Long id) {
        DeviceMonitorDTO deviceInfo = deviceService.getDeviceInfo(id);
        return ResponseEntity.ok(deviceInfo);
    }

//    // FCM 토큰을 받아서 저장하는 API
//    @Operation(summary = "앱 토큰 저장", description = "사용할 앱의 토큰을 서버에 저장함")
//    @PostMapping("/register")
//    public ResponseEntity<String> registerDevice(@RequestBody UserDTO userDTO) {
//        deviceService.registerToken(userDTO);
//        return ResponseEntity.ok("Device registered successfully");
//    }


    @Operation(summary = "기기 상태 업데이트", description = "외출 여부를 저장함")
    @PutMapping("device/{id}/status")
    @Parameter(name = "deviceStatus", description = "0이면 귀가, 1이면 외출")
    public ResponseEntity<String> updateDeviceStatus(@PathVariable Long id, @RequestParam Boolean deviceStatus) {
        deviceService.updateDeviceStatus(id, deviceStatus);
        return ResponseEntity.ok("외출 상태가 업데이트되었습니다.");
    }
}
//    @GetMapping("/all")
//    @Operation(summary = "전체 단말기 조회", description = "등록된 단말기를 모두 조회함")
//    public ResponseEntity<List<DeviceManageDTO>> getAllDevices() {
//        List<DeviceManageDTO> devices = deviceService.findAllDevices();
//        return ResponseEntity.ok(devices);
//    }


