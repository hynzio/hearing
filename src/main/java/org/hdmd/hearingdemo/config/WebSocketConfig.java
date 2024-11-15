package org.hdmd.hearingdemo.config;
import org.hdmd.hearingdemo.service.MqttCommandSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MqttCommandSender mqttCommandSender;

    @Autowired
    public WebSocketConfig(MqttCommandSender mqttCommandSender) {
        this.mqttCommandSender = mqttCommandSender;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandler handler = new org.hdmd.hearingdemo.handler.WebSocketHandler(mqttCommandSender);
        registry.addHandler(handler, "/demo").setAllowedOrigins("*");
    }
}
