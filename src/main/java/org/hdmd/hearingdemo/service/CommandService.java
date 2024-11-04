package org.hdmd.hearingdemo.service;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

@Service
public class CommandService {

    private static final String RASPBERRY_PI_URL = "http://<라즈베리파이 IP>:<포트>/command";

    public void sendCommandToRaspberryPi(String command) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(RASPBERRY_PI_URL);
            String jsonCommand = """
                {
                    "command": "%s"
                }
                """.formatted(command);

            httpPut.setEntity(new StringEntity(jsonCommand));
            httpPut.setHeader("Content-Type", "application/json");

            var response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("명령 전송 성공: " + command);
            } else {
                System.out.println("명령 전송 실패. 상태 코드: " + statusCode);
            }
        } catch (Exception e) {
            System.err.println("명령 전송 중 오류 발생: " + e.getMessage());
        }
    }
}
