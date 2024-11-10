package org.hdmd.hearingdemo.handler;

import lombok.Setter;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    // clientType을 설정하는 메소드
    @Setter
    private String clientType;
    private final Set<WebSocketSession> androidSessions = new HashSet<>();  // 안드로이드 세션 관리
    private final Set<WebSocketSession> raspberrySessions = new HashSet<>();  // 라즈베리 파이 세션 관리

    @PostConstruct
    public void init() {
        // WebSocketHandler의 초기화 작업
        logger.info("WebSocketHandler 초기화됨");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("{} 연결됨: {}", clientType, session.getId());

        // 클라이언트 타입에 따라 세션 저장
        if ("ANDROID".equals(clientType)) {
            androidSessions.add(session);
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.add(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("{}로부터 메시지 받음: {}", clientType, message.getPayload());

        // 메시지 처리 로직 추가
        if ("ANDROID".equals(clientType)) {
            // 안드로이드 클라이언트에서 처리할 메시지 로직
        } else if ("RASPBERRY".equals(clientType)) {
            // 라즈베리 파이 클라이언트에서 처리할 메시지 로직
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("{} 연결 종료됨: {}", clientType, session.getId());

        // 클라이언트 타입에 따라 세션 제거
        if ("ANDROID".equals(clientType)) {
            androidSessions.remove(session);
        } else if ("RASPBERRY".equals(clientType)) {
            raspberrySessions.remove(session);
        }
    }

    // 위치 데이터 방송
    public void broadcastLocationData(Object locationData) {
        // 라즈베리 파이 세션에만 위치 데이터 전송
        if ("RASPBERRY".equals(clientType)) {
            for (WebSocketSession session : raspberrySessions) {
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

    // 안드로이드 세션에 위치 데이터 전송
    public void sendLocationToAndroid(Object locationData) {
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

    // 세션을 종료시키는 메소드
    public void closeSessions() {
        if ("ANDROID".equals(clientType)) {
            for (WebSocketSession session : androidSessions) {
                try {
                    session.close();
                } catch (Exception e) {
                    logger.error("세션 종료 실패", e);
                }
            }
        } else if ("RASPBERRY".equals(clientType)) {
            for (WebSocketSession session : raspberrySessions) {
                try {
                    session.close();
                } catch (Exception e) {
                    logger.error("세션 종료 실패", e);
                }
            }
        }
    }

}
