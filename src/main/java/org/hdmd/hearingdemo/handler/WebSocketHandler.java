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
        // Logging successful connection
        logger.info("WebSocket 연결 성공, 세션 ID: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Parsing the incoming message to extract clientId and action
        String payload = message.getPayload();
        LocationData messageData = objectMapper.readValue(payload, LocationData.class);

        String action = messageData.getAction();
        String clientId = messageData.getClientId();

        if (clientId != null) {
            // Saving clientId to the session attributes
            session.getAttributes().put("clientId", clientId);

            // Registering the session based on the clientId
            sessions.put(clientId, session);

            if ("ANDROID".equals(clientId)) {
                // Sending start command via MQTT when an Android client connects
                mqttCommandSender.sendStartCommand();
                logger.info("안드로이드 클라이언트 연결: {}", clientId);
            }
            logger.info("클라이언트 연결 확인: {}, 세션 ID: {}", clientId, session.getId());
        }

        // Handling location data from Raspberry Pi
        if ("sendLocation".equals(action) && "RASPBERRY".equals(clientId)) {
            logger.info("라즈베리파이 위치 데이터 수신: {}", messageData);

            // Sending location data to the Android session
            WebSocketSession androidSession = sessions.get("ANDROID");
            if (androidSession != null && androidSession.isOpen()) {
                androidSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageData)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Removing the session when the connection is closed
        String clientId = (String) session.getAttributes().get("clientId");

        if (clientId != null) {
            if ("ANDROID".equals(clientId)) {
                // Sending stop command via MQTT when the Android session disconnects
                mqttCommandSender.sendStopCommand();
            }
            sessions.remove(clientId);
            logger.info("WebSocket 연결 종료: {}, 세션 ID: {}", clientId, session.getId());
        }
    }

    // Method to get the session using clientId
    public WebSocketSession getSession(String clientId) {
        return sessions.get(clientId);
    }
}