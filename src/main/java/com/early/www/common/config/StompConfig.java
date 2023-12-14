package com.early.www.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
public class StompConfig implements WebSocketMessageBrokerConfigurer{

	
	/*
	 * "/earlyshake" is the HTTP URL for the endpoint to which a WebSocket (or
	 * SockJS) client will need to connect to for the WebSocket handshake.
	 * 
	 * STOMP messages whose destination header begins with "/app" are routed
	 * to @MessageMapping methods in @Controller classes.
	 * 
	 * Use the built-in, message broker for subscriptions and broadcasting; Route
	 * messages whose destination header begins with "/topic" or "/queue" to the
	 * broker.
	 * 
	 * "topic" 1:N, "queue" 1:1
	 */
	
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    	// handshake url 
        registry.addEndpoint("/earlyshake").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
    }


}
