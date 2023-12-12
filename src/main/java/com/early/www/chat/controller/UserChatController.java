package com.early.www.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.service.ChatService;

@RestController
public class UserChatController {

	@Autowired
	ChatService service;
	
	
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(){
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		JSONArray jsonArr = new JSONArray();
		
		service.getMyChatList(null);
		
		
		resultMap.put("chat_list", jsonArr.toJSONString());
		
		return resultMap;
	}
	
	
}
