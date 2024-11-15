package org.hdmd.hearingdemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public class MqttCommandSender {

    @Autowired
    private MessageChannel mqttOutboundChannel;

    public void sendStartCommand() {
        String command = "start";
        sendCommand(command);
    }

    public void sendStopCommand() {
        String command = "stop";
        sendCommand(command);
    }

    private void sendCommand(String command) {
        Message<String> mqttMessage = MessageBuilder
                .withPayload(command)
                .setHeader(MqttHeaders.TOPIC, "raspberry/control")
                .build();
        mqttOutboundChannel.send(mqttMessage);
    }
}
