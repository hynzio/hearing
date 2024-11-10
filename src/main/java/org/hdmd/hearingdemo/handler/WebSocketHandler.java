package org.hdmd.hearingdemo.handler;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.hdmd.hearingdemo.model.LocationData;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Setter
    private String clientType;
    private final Set<WebSocketSession> androidSessions = new HashSet<>();  // 안드로이드 세션 관리
    private final Set<WebSocketSession> raspberrySessions = new HashSet<>();  // 라즈베리 파이 세션 관리
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용 ObjectMapper

    // WebSocketHandler 초기화 작업
    @PostConstruct
    public void init() {
        logger.info("WebSocketHandler 초기화됨");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("{} 연결됨: {}", clientType, session.getId());

        if ("ANDROID".equals(clientType)) {
            androidSessions.add(session);
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.add(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 라즈베리 파이로부터 받은 위치 데이터 처리
        if ("RASPBERRY".equals(clientType)) {
            String payload = message.getPayload();
            logger.info("라즈베리 파이로부터 위치 데이터 받음: {}", payload);

            // JSON 파싱하여 LocationData 객체로 변환
            LocationData locationData = objectMapper.readValue(payload, LocationData.class);
            logger.info("파싱된 위치 데이터: {}", locationData);

            // 안드로이드 세션에 위치 데이터 전송
            sendLocationToAndroid(locationData);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("{} 연결 종료됨: {}", clientType, session.getId());

        if ("ANDROID".equals(clientType)) {
            androidSessions.remove(session);
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.remove(session);
        }
    }

    // 안드로이드 세션에 위치 데이터 전송
    public void sendLocationToAndroid(LocationData locationData) {
        if ("ANDROID".equals(clientType)) {
            for (WebSocketSession session : androidSessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(locationData.toString()));
                    } catch (Exception e) {
                        logger.error("위치 데이터 전송 실패", e);
                    }
                }
            }
        }
    }

    // 안드로이드 세션 종료
    public void disconnectAndroidSession() {
        for (WebSocketSession session : androidSessions) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("안드로이드 세션 종료 실패", e);
            }
        }
        androidSessions.clear();
    }

    // 라즈베리 파이 세션 종료
    public void disconnectRaspberrySession() {
        for (WebSocketSession session : raspberrySessions) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("라즈베리 파이 세션 종료 실패", e);
            }
        }
        raspberrySessions.clear();
    }
}
