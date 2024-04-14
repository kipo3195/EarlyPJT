package com.early.www.common.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.chat.service.ChatService;
import com.early.www.common.service.CommonService;
import com.early.www.properties.OAuthProperties;
import com.early.www.properties.TestProperties;
import com.early.www.user.model.EarlyUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CommonRestController {

	@Autowired
	CommonService service;
	
	@Autowired
	TestProperties testProperties;
	
	@Autowired
	OAuthProperties oAuthProperties;
	
	@Autowired
	ChatService chatService;
	
	
	// 회원가입 전 ID 중복 체크 API
	@GetMapping("/join/idDupCheck")
	@ResponseBody
	public Map<String, String> idDupCheck(@RequestParam String username) {
		
		log.info("[/join/idDupCheck] check user id : {}", username);
		
		boolean result = service.existsUsername(username);
		
		Map<String, String> resultMap = new HashMap<>();
		
		if(result) {
			// 중복
			resultMap.put("result", "true");
		}else {
			resultMap.put("result", "false");
		}
		
		log.info("[/join/idDupCheck] check user id : {} : result : {}", username, result ? "already exists":"available");
		return resultMap;
	}
	
	
	// 회원가입 요청 API
	// react 버전 axios를 통한 통신으로 데이터 전송시 json 형태로 들어오기 때문에 @RequestBody 어노테이션 필요함.
	@PostMapping("/join")
	public Map<String, String> joinRequest(@RequestBody EarlyUser user) {
		
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		String result = service.userJoin(user);
		if(result.equals("success")) {
			resultMap.put("flag", "success");
		}else {
			resultMap.put("flag", "fail");
		}
		
		log.info("[/join] user id : {} result : {}", user.getUsername(), result.equals("success") ? "success":"fail");
		return resultMap;
	}
	
	
	// 로그인 요청 API
	@PostMapping("/login")
	@ResponseBody
	public Map<String, String> loginRequest(String username, String password, String token, HttpServletResponse response, HttpServletRequest request) {
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("Authorization") != null) {
			username = (String) request.getAttribute("username");
			// 채팅, 쪽지의 미확인 건수 조회 
			Map<String, String> allUnreadMap = chatService.getAllUnreadCnt(username);
			
			resultMap.put("flag", "success");
			resultMap.put("token", response.getHeader("Authorization"));
			resultMap.put("chat", allUnreadMap.get("chat"));
			
			//resultMap.put("msg", allUnreadMap.get("msg"));
		}else {
			resultMap.put("flag", "fail");
			
		}
		return resultMap;
		
	}
	
	
	
	
	
	
	
	
	@GetMapping("/testRedis")
	public void testSet(@RequestParam String data) {
		
		service.testRedisSet(data);
		
	}

	@GetMapping("/testRedisGet")
	public void testGet(@RequestParam String data) {
		
		service.testRedisGet(data);
		
	}
	
	@GetMapping("/test")
	public void test() {
//		System.out.println(testProperties.getStringConfig());
//		System.out.println(testProperties.getIntConfig());
//		System.out.println(testProperties.getListConfig());
//		System.out.println(testProperties.getMapConfig());
//		System.out.println(oAuthProperties.getClientId());
//		System.out.println(oAuthProperties.getGoogleLoginUrl());
	}
	
}
