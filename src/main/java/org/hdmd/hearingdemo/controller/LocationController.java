package org.hdmd.hearingdemo.controller;

import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private MqttCommandSender mqttCommandSender;

    @PostMapping("/start")
    public ResponseEntity<String> start() {
        mqttCommandSender.sendStartCommand();
        return ResponseEntity.ok("명령 전송 성공");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        mqttCommandSender.sendStopCommand();
        return ResponseEntity.ok("명령 전송 성공");
    }
}