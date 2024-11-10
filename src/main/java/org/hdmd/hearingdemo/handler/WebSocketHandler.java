package org.hdmd.hearingdemo.handler;

import org.hdmd.hearingdemo.model.LocationData;
import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private MqttCommandSender mqttCommandSender;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        System.out.println("Android 클라이언트 연결됨: " + session.getId());

        // MQTT로 라즈베리파이에 start 명령 전송
        mqttCommandSender.sendStartCommand();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        System.out.println("Android 클라이언트 연결 종료됨: " + session.getId());

        // MQTT로 라즈베리파이에 stop 명령 전송
        mqttCommandSender.sendStopCommand();
    }

    public void broadcastLocationData(LocationData locationData) {
        String locationMessage = String.format(""" 
        {
            "latitude": %8f,
            "longitude": %8f,
            "timestamp": "%s"
        }
        """, locationData.getLatitude(), locationData.getLongitude(), locationData.getTimestamp());

        sessionMap.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(locationMessage));
                } catch (Exception e) {
                    System.err.println("위치 데이터 전송 오류: " + e.getMessage());
                }
            }
        });
    }
}
