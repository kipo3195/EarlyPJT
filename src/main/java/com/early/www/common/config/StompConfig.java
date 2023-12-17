package com.early.www.common.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
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
    	// websocket(또는 sockJS) 클라이언트가 연결을 하기위한 handshake용 url.
        registry.addEndpoint("/earlyShake")
        		.withSockJS()
        		.setStreamBytesLimit(512 * 1024) //512KB
        		.setHttpMessageCacheSize(1000)
        		.setDisconnectDelay(5 * 1000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
        
    }

	
	

}
