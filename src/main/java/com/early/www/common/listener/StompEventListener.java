package com.early.www.common.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class StompEventListener {

	 // 20231224
	 // StompJs를 통해 connect/disconnect 되는 사용자의 정보 로깅
	
	    @EventListener
	    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

	        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
	        String sessionId = headerAccessor.getSessionId();

	        System.out.println("[Connected] websocket session id "+ sessionId);
	    }

	    @EventListener
	    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

	        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
	        String sessionId = headerAccessor.getSessionId();

	        System.out.println("[DisConnected] websocket session id "+ sessionId);
	    }
	
}
