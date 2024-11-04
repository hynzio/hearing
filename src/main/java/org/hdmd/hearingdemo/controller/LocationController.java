package org.hdmd.hearingdemo.controller;

import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.hdmd.hearingdemo.model.LocationData;
import org.hdmd.hearingdemo.service.RaspberryPiCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api1/location")
public class LocationController {

    @Autowired
    private RaspberryPiCommandService raspberryPiCommandService;

    @Autowired
    private WebSocketHandler locationWebSocketHandler;

    @PostMapping("/connect")
    public ResponseEntity<String> connect() {
        try {
            raspberryPiCommandService.sendStartCommand();
            return ResponseEntity.ok("START 명령 전송 성공");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("START 명령 전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        try {
            raspberryPiCommandService.sendStopCommand();
            return ResponseEntity.ok("STOP 명령 전송 성공");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("STOP 명령 전송 실패: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> receiveLocationData(@RequestBody LocationData locationData) throws IOException {
        locationWebSocketHandler.broadcastLocationData(locationData);
        return ResponseEntity.ok("위치 데이터 수신 및 브로드캐스트 성공");
    }
}
