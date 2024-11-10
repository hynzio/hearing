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
        return new WebSocketHandler("ANDROID");  // 클라이언트 타입을 "ANDROID"로 설정
    }

    @Bean
    public WebSocketHandler raspberryWebSocketHandler() {
        return new WebSocketHandler("RASPBERRY");  // 클라이언트 타입을 "RASPBERRY"로 설정
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 안드로이드 클라이언트에 대해 엔드포인트 등록
        registry.addHandler(androidWebSocketHandler(), "/demo").setAllowedOrigins("*");

        // 라즈베리 파이 클라이언트에 대해 다른 엔드포인트 등록
        registry.addHandler(raspberryWebSocketHandler(), "/demo/location").setAllowedOrigins("*");
    }
}
