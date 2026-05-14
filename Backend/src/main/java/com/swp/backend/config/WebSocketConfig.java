package com.swp.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic để nhận thông báo/tin nhắn (Subscribe)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix để gửi tin nhắn từ Client lên (Send)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // URL để React kết nối: ws://localhost:8080/api/v1/ws
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS(); // Hỗ trợ fallback nếu browser cũ
    }
}