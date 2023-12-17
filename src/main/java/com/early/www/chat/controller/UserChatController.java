package com.early.www.chat.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.model.ChatMain;
import com.early.www.chat.model.ChatRoom;
import com.early.www.chat.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserChatController {

	@Autowired
	ChatService service;
	
	
	
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(HttpServletRequest request){
		
		String username = (String) request.getAttribute("username");

		System.out.println("[UserChatController] user name is : "+ username);
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(username != null && !username.isEmpty()) {
			List<ChatRoom> chatList = service.getMyChatList(username);
			if(chatList != null && !chatList.isEmpty()) {
				
				ObjectMapper mapper = new ObjectMapper();

				List<String> list = new ArrayList<String>();
				
				// 객체를 json형태의 String으로 변환 
				for(int i = 0; i < chatList.size(); i++) {
					ChatRoom chatObj = chatList.get(i);
					
					try {
						list.add(mapper.writeValueAsString(chatObj));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
				
				System.out.println(list);
				
				
				resultMap.put("chat_list", list.toString());
			}else {
				// 채팅리스트가 없음 
				resultMap.put("chat_list", "C404");
			}
		}else {
			resultMap.put("chat_list", "C403");
		}
		
		return resultMap;
	}
	
	
	private final SimpMessagingTemplate simpMessagingTemplate;
	
	@MessageMapping("/test/message")
	public String handle(ChatMain main) {
		
		System.out.println(main);
		
		String recvIds[] = main.getChatReceiver().split("[|]");
		
		for(int i = 0; i< recvIds.length; i++) {
			String dest = "/sub/channels/"+recvIds[i];
			System.out.println(dest);
			simpMessagingTemplate.convertAndSend(dest, main.getChatContents());
			
		}
		
		return "";
	}

	
	
	
}
