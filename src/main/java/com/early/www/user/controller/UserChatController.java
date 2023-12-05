package com.early.www.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserChatController {

	
	@PostMapping("/user/chatList")
	public Map<String, String> chatList(){
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		
		JSONObject resultJson = new JSONObject();
		resultJson.put("chat_participants", "user1|user2");
		resultJson.put("chat_room_title", "test room");
		resultJson.put("room_key", "a1b2c3");
		
		resultMap.put("room_info", resultJson.toJSONString());
		
		
		return resultMap;
	}
	
	
}
