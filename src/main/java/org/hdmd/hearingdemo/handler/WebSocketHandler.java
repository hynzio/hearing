package org.hdmd.hearingdemo.handler;

import org.hdmd.hearingdemo.model.LocationData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final String clientType;
    private final ConcurrentHashMap<String, WebSocketSession> androidSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> raspberrySessions = new ConcurrentHashMap<>();

    public WebSocketHandler(String clientType) {
        this.clientType = clientType;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("clientType", clientType);

        // 클라이언트 타입에 따라 세션을 관리
        if ("ANDROID".equals(clientType)) {
            androidSessions.put(session.getId(), session);
            System.out.println("Android 클라이언트 연결됨: " + session.getId());
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.put(session.getId(), session);
            System.out.println("Raspberry Pi 클라이언트 연결됨: " + session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String clientType = (String) session.getAttributes().get("clientType");

        // Raspberry Pi로부터 받은 데이터를 Android로 브로드캐스트
        if ("RASPBERRY".equals(clientType)) {
            String payload = message.getPayload();
            System.out.println("Received from Raspberry Pi: " + payload);

            androidSessions.values().forEach(androidSession -> {
                if (androidSession.isOpen()) {
                    try {
                        androidSession.sendMessage(new TextMessage(payload));
                    } catch (Exception e) {
                        System.err.println("위치 데이터 전송 오류: " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String clientType = (String) session.getAttributes().get("clientType");

        // 클라이언트 타입에 따라 세션을 관리
        if ("ANDROID".equals(clientType)) {
            androidSessions.remove(session.getId());
            System.out.println("Android 클라이언트 연결 종료됨: " + session.getId());
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.remove(session.getId());
            System.out.println("Raspberry Pi 클라이언트 연결 종료됨: " + session.getId());
        }
    }

    // 안드로이드 세션으로 위치 데이터 브로드캐스트
    public void broadcastLocationData(LocationData locationData) {
        String locationJson = String.format("{\"latitude\": \"%f\", \"longitude\": \"%f\", \"timestamp\": \"%s\"}",
                locationData.getLatitude(), locationData.getLongitude(), locationData.getTimestamp());

        androidSessions.values().forEach(androidSession -> {
            if (androidSession.isOpen()) {
                try {
                    androidSession.sendMessage(new TextMessage(locationJson));
                } catch (Exception e) {
                    System.err.println("위치 데이터 전송 중 오류: " + e.getMessage());
                }
            }
        });
    }

    // 현재 Raspberry Pi 세션 종료
    public void closeRaspberrySessions() {
        raspberrySessions.values().forEach(session -> {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("Raspberry 세션 종료 중 오류: " + e.getMessage());
            }
        });
        raspberrySessions.clear();
    }
}
