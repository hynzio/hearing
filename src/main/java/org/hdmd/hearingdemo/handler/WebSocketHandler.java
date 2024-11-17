package org.hdmd.hearingdemo.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hdmd.hearingdemo.model.LocationData;
import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final MqttCommandSender mqttCommandSender;

    public WebSocketHandler(MqttCommandSender mqttCommandSender) {
        this.mqttCommandSender = mqttCommandSender;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 클라이언트 ID는 첫 번째 메시지를 통해 추출
        logger.info("WebSocket 연결 성공, 세션 ID: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 첫 번째 메시지를 통해 clientId 추출
        String payload = message.getPayload();
        LocationData messageData = objectMapper.readValue(payload, LocationData.class);

        String action = messageData.getAction();
        String clientId = messageData.getClientId();

        if (clientId != null) {
            // 세션에 clientId를 저장
            session.getAttributes().put("clientId", clientId);

            // 클라이언트 ID에 따라 세션을 등록
            sessions.put(clientId, session);

            if ("ANDROID".equals(clientId)) {
                mqttCommandSender.sendStartCommand();  // 안드로이드 클라이언트 연결 시 MQTT로 라즈베리파이에 start 명령 전송
                logger.info("안드로이드 클라이언트 연결: {}", clientId);
            }
            logger.info("클라이언트 연결 확인: {}, 세션 ID: {}", clientId, session.getId());
        }

        // 위치 데이터를 처리하는 부분
        if ("sendLocation".equals(action) && "RASPBERRY".equals(clientId)) {
            logger.info("라즈베리파이 위치 데이터 수신: {}", messageData);

            // 안드로이드 세션으로 위치 데이터 전송
            WebSocketSession androidSession = sessions.get("ANDROID");
            if (androidSession != null && androidSession.isOpen()) {
                androidSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageData)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 연결 종료 시 clientId에 해당하는 세션을 제거
        String clientId = (String) session.getAttributes().get("clientId");

        if (clientId != null) {
            if ("ANDROID".equals(clientId)) {
                mqttCommandSender.sendStopCommand();  // 안드로이드 세션 종료 시 MQTT로 라즈베리파이에 stop 명령 전송
            }
            sessions.remove(clientId);
            logger.info("WebSocket 연결 종료: {}, 세션 ID: {}", clientId, session.getId());
        }
    }

    // 세션을 클라이언트 ID로 찾을 수 있는 메서드 추가
    public WebSocketSession getSession(String clientId) {
        return sessions.get(clientId);
    }
}
