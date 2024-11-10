package org.hdmd.hearingdemo.config;

import org.hdmd.hearingdemo.handler.WebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public WebSocketHandler androidWebSocketHandler() {
        WebSocketHandler handler = new WebSocketHandler();
        handler.setClientType("ANDROID");  // 안드로이드 타입 설정
        return handler;
    }

    @Bean
    public WebSocketHandler raspberryWebSocketHandler() {
        WebSocketHandler handler = new WebSocketHandler();
        handler.setClientType("RASPBERRY");  // 라즈베리 파이 타입 설정
        return handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(androidWebSocketHandler(), "/demo").setAllowedOrigins("*");
        registry.addHandler(raspberryWebSocketHandler(), "/demo/location").setAllowedOrigins("*");
    }
}