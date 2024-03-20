package com.early.www.common.listener;

import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;

@Component
public class StompEventListener {
	
		// StompJs를 통해 connect/disconnect 되는 사용자의 정보 로깅
		Logger regLogger = org.slf4j.LoggerFactory.getLogger("WebSocketLogger");
	
	    @EventListener
	    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

	        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
	        String sessionId = headerAccessor.getSessionId();

	        regLogger.info("[Connected] websocket session id {}", sessionId);
	        
	    }

	    @EventListener
	    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

	        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
	        String sessionId = headerAccessor.getSessionId();

	        regLogger.info("[DisConnected] websocket session id {}", sessionId);
	    }
	    
	    
	
}
