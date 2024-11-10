package org.hdmd.hearingdemo.handler;

import lombok.Data;
import org.hdmd.hearingdemo.model.LocationData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private String clientType;  // 필드로 설정

    private final ConcurrentHashMap<String, WebSocketSession> androidSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> raspberrySessions = new ConcurrentHashMap<>();

    // 기본 생성자
    public WebSocketHandler() {
        this.clientType = "DEFAULT";  // 기본 값 설정 (필요시 수정)
    }

    // clientType을 설정하는 setter 메서드
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // clientType을 session의 속성으로 설정하고 연결 처리
        session.getAttributes().put("clientType", clientType);
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

        if ("RASPBERRY".equals(clientType)) {
            String payload = message.getPayload();
            System.out.println("Received from Raspberry Pi: " + payload);

            // 라즈베리 파이에서 수신한 메시지를 안드로이드 클라이언트에 브로드캐스트
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

    public void closeCurrentSession() {
        raspberrySessions.values().forEach(session -> {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("라즈베리 세션 종료 중 오류: " + e.getMessage());
            }
        });
        raspberrySessions.clear();
    }
}
