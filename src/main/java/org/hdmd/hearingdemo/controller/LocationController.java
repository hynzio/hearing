package org.hdmd.hearingdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.hdmd.hearingdemo.model.LocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "실시간 위치", description = "단말기 현재위치 확인")
@RestController
@RequestMapping("/api1/location")
public class LocationController {

    @Autowired
    private WebSocketHandler locationWebSocketHandler;

    @PostMapping("/connect")
    @Operation(
            summary = "웹소켓 연결",
            description = "실시간 위치 확인을 위한 웹소켓 통신 연결",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<String> connect() {
        return ResponseEntity.ok("웹소켓 연결됨");
    }

    @PostMapping("/disconnect")
    @Operation(
            summary = "연결 종료",
            description = "GPS화면 웹소켓 연결 종료",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<String> disconnect() {
        return ResponseEntity.ok("웹소켓 연결 종료됨");
    }

    @PostMapping
    @Operation(
            summary = "GPS값 업데이트",
            description = "실시간 위치값 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 조회됨"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")})
    public ResponseEntity<String> receiveLocationData(@RequestBody LocationData locationData) throws IOException {
        locationWebSocketHandler.broadcastLocationData(locationData);
        return ResponseEntity.ok("위치 데이터 수신");
    }
}
