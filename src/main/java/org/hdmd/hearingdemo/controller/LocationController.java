package org.hdmd.hearingdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.hdmd.hearingdemo.model.LocationData;
import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "실시간 위치", description = "단말기 현재위치 확인")
@RestController
@RequestMapping("/api1/location")
public class LocationController {

    @Autowired
    private WebSocketHandler androidWebSocketHandler;  // 안드로이드 WebSocketHandler

    @Autowired
    private WebSocketHandler raspberryWebSocketHandler;  // 라즈베리 파이 WebSocketHandler

    @Autowired
    private MqttCommandSender mqttCommandSender;

    @PostMapping("/connect")
    @Operation(
            summary = "웹소켓 연결",
            description = "실시간 위치 확인을 위한 웹소켓 통신 연결",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 연결됨"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    public ResponseEntity<String> connect() {
        try {
            // 안드로이드 연결 시 라즈베리 파이에 시작 명령 전송
            mqttCommandSender.sendStartCommand();
            return ResponseEntity.ok("웹소켓 및 MQTT 연결 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("연결 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    @Operation(
            summary = "연결 종료",
            description = "GPS화면 웹소켓 연결 종료",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 연결 종료"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    public ResponseEntity<String> disconnect() {
        try {
            // 안드로이드 연결 종료
            androidWebSocketHandler.disconnectAndroidSession();
            // 라즈베리 파이 세션 종료
            raspberryWebSocketHandler.disconnectRaspberrySession();
            // 라즈베리 파이에 정지 명령 전송
            mqttCommandSender.sendStopCommand();
            return ResponseEntity.ok("웹소켓 및 MQTT 연결 종료됨");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("연결 종료 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(
            summary = "GPS값 업데이트",
            description = "실시간 위치값 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "위치 데이터 수신"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    public ResponseEntity<String> sendLocationData(@RequestBody LocationData locationData) {
        try {
            // 위치 데이터 안드로이드와 라즈베리 파이에 전송
            androidWebSocketHandler.sendLocationToAndroid(locationData);
            return ResponseEntity.ok("위치 데이터 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("위치 데이터 전송 실패: " + e.getMessage());
        }
    }
}
