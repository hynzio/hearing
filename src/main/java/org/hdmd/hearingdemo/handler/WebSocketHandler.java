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
        String clientId = (String) session.getAttributes().get("clientId");

        if (clientId != null) {
            sessions.put(clientId, session);  // 클라이언트 아이디를 키로 세션 등록

            if ("ANDROID".equals(clientId)) {
                mqttCommandSender.sendStartCommand();  // 안드로이드 세션 연결 시 MQTT로 라즈베리파이에 start 명령 전송
            }

            logger.info("WebSocket 연결 성공: {}", clientId);
        } else {
            logger.error("클라이언트 아이디가 설정되지 않았습니다.");
            session.close(CloseStatus.BAD_DATA);  // 클라이언트 아이디가 없으면 연결 종료
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        LocationData messageData = objectMapper.readValue(payload, LocationData.class);  // 한 번만 파싱

        String action = messageData.getAction();
        String clientId = messageData.getClientId();

        if ("sendLocation".equals(action) && "RASPBERRY".equals(clientId)) {
            logger.info("라즈베리파이 위치 데이터 수신: {}", messageData);

            WebSocketSession androidSession = sessions.get("ANDROID");
            if (androidSession != null && androidSession.isOpen()) {
                androidSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageData)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String clientId = (String) session.getAttributes().get("clientId");

        if (clientId != null) {
            if ("ANDROID".equals(clientId)) {
                mqttCommandSender.sendStopCommand();
            }
            sessions.remove(clientId);
            logger.info("WebSocket 연결 종료: {}", clientId);
        }
    }
}