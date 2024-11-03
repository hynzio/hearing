package org.hdmd.hearingdemo.controller;

import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.dto.LocationDataDTO;
import org.hdmd.hearingdemo.handler.WebsocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final WebsocketHandler websocketHandler;

//    @PostMapping("/send")
//    public ResponseEntity<Void> sendLocation(@RequestBody LocationDataDTO locationData) {
//        // 핸들러를 통해 라즈베리파이로 위치 데이터 전송
//        websocketHandler.receiveLocationData(locationData.getLocation());
//        return ResponseEntity.ok().build();
//    }
}
