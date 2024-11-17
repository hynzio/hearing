package org.hdmd.hearingdemo.controller;

import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("location")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private WebSocketHandler webSocketHandler;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public void WebSocketController(MqttCommandSender mqttCommandSender) {
        this.webSocketHandler = new WebSocketHandler(mqttCommandSender);
    }


    // $connect 라우팅 처리
    @PostMapping("/connect")
    public ResponseEntity<String> connect(@RequestParam String clientId) {
        try {
            logger.info("WebSocket 연결 성공: {}", clientId);
            return ResponseEntity.ok("WebSocket 연결 성공: " + clientId);
        } catch (Exception e) {
            logger.error("WebSocket 연결 실패: {}", clientId, e);
            return ResponseEntity.status(500).body("WebSocket 연결 실패: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect(@RequestParam String clientId) {
        WebSocketSession session = sessions.get(clientId);

        if (session != null) {
            try {
                // 연결 종료 처리
                webSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);
                sessions.remove(clientId);  // 세션 제거

                logger.info("WebSocket 연결 종료: {}", clientId);
                return ResponseEntity.ok("WebSocket 연결 종료: " + clientId);
            } catch (Exception e) {
                logger.error("WebSocket 연결 종료 실패: {}", clientId, e);
                return ResponseEntity.status(500).body("WebSocket 연결 종료 실패: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(404).body("WebSocket 세션을 찾을 수 없습니다: " + clientId);
        }
    }
}