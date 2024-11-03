package org.hdmd.hearingdemo.handler;

import lombok.NonNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebsocketHandler extends TextWebSocketHandler {

    private static final String RASPBERRY_PI_URL = "http://<라즈베리파이 IP>:<포트>/api/location";

    // 연결된 클라이언트 세션들을 저장하는 맵 (여러 클라이언트와의 WebSocket 연결을 관리)
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        // 세션이 열리면 세션을 저장
        sessionMap.put(session.getId(), session);
        System.out.println("클라이언트 연결됨: " + session.getId());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.equals("START")) {
            sendCommandToRaspberryPi("start");
        } else if (payload.equals("STOP")) {
            sendCommandToRaspberryPi("stop");
        }
    }

    private void sendCommandToRaspberryPi(String command) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(RASPBERRY_PI_URL);

            String jsonCommand = """
            {
                "command": "%s"
            }
            """.formatted(command);

            StringEntity entity = new StringEntity(jsonCommand);
            httpPut.setEntity(entity);
            httpPut.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(httpPut);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("위치 전송 명령 성공: " + command);
            } else {
                System.out.println("명령 전송 실패. 상태 코드: " + statusCode);
            }
        } catch (IOException e) {
            System.err.println("명령 전송 중 오류 발생: " + e.getMessage());
            throw e; //테스트 하며 발생하는 오류에 따라 예외 추가 필요
        }
    }

    // 라즈베리파이에서 HTTP로 위치 데이터를 받는 엔드포인트 (예시)
    public void receiveLocationData(String locationData) throws Exception {
        // 받은 위치 데이터를 모든 클라이언트에게 전송
        for (WebSocketSession session : sessionMap.values()) {
            sendLocation(session, locationData);
        }
    }

    // 라즈베리파이에서 전송된 위치 데이터를 클라이언트에게 전송하는 메서드
    public void sendLocation(WebSocketSession session, String locationData) throws Exception {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(locationData));
            System.out.println("위치 데이터 전송: " + locationData);
        } else {
            System.out.println("세션이 열려있지 않음: 위치 데이터를 전송할 수 없습니다.");
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) throws Exception {
        sessionMap.remove(session.getId());
        System.out.println("연결 종료 " + session.getId());
    }
}
