package org.hdmd.hearingdemo.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hdmd.hearingdemo.model.LocationData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("클라이언트 연결됨: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("클라이언트 연결 종료: " + session.getId());
    }

    public void broadcastLocationData(LocationData locationData) throws IOException {
        // LocationData 객체를 JSON 형식으로 변환
        String locationJson = objectMapper.writeValueAsString(locationData);

        // 모든 연결된 세션에 위치 데이터 전송
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(locationJson));
            }
        }

        System.out.println("위치 데이터 전송: " + locationJson);
    }
}
