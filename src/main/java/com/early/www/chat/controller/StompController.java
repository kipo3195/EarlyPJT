package com.early.www.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

	@MessageMapping("/test/message")
	public String handle(String message) {
		
		System.out.println("message : " + message);
		
		return message;
		
	}
	
}
