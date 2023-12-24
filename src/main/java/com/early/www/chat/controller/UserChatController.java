package com.early.www.chat.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
	ChatService chatService;
	
	// 채팅방 리스트 조회 
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(HttpServletRequest request){
		
		String username = (String) request.getAttribute("username");

		System.out.println("[UserChatController] user name is : "+ username);
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(username != null && !username.isEmpty()) {
			List<ChatRoom> chatList = chatService.getMyChatList(username);
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
	public void handle(ChatMain main) {
		
		String recvIds[] = main.getChatReceiver().split("[|]");
		
		// DB 저장
		chatService.putChatMain(main);
		
		System.out.println( main.getChatContents());
		// STOMP를 통한 데이터 전송
		for(int i = 0; i< recvIds.length; i++) {
			
			// 여기 나중에 처리하기  
			if(!main.getChatSender().toLowerCase().equals(recvIds[i].toLowerCase())) {
				String dest = "/topic/user/"+recvIds[i];
				simpMessagingTemplate.convertAndSend(dest, main.getChatContents());
			}
		}
	}
	
	
}
