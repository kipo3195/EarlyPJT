package com.early.www.chat.handler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketHandler extends TextWebSocketHandler{
// 얘 사용안하는듯
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
	
		System.out.println("[WebSocketHandler] session : " + session + "message : "+ message);
	
	}

	
}
