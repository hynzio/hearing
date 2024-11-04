package org.hdmd.hearingdemo.service;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RaspberryPiCommandService {

    private static final String RASPBERRY_PI_URL = "http://<라즈베리파이 IP>:<포트>/api/location";

    public void sendStartCommand() throws IOException {
        sendCommandToRaspberryPi("start");
    }

    public void sendStopCommand() throws IOException {
        sendCommandToRaspberryPi("stop");
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

            httpClient.execute(httpPut);
        }
    }
}
