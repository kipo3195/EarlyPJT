package com.early.www.user.controller;

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
import com.early.www.user.model.EarlyUser;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {
	
	@Autowired
	CommonService service;
	
	@Autowired
	ChatService chatService;

	// react axios 테스트
	@GetMapping("/user/random")
	public Map<String, String> test(HttpServletResponse response) {
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("error_code") != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			resultMap.put("flag", "success");
		}
		return resultMap;
	}
	
	// react axios 테스트
	@GetMapping("/api/random")
	public Map<String, Double> random() {
		Map<String, Double> resultMap = new HashMap<String, Double>();
		
		double random = Math.random();
		resultMap.put("data", random);
		return resultMap;
	}
	
	// access token 재발급 API
	@PostMapping("/user/accessToken")
	public Map<String, String> accessToken(HttpServletResponse response){
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("error_code") != null) {
			resultMap.put("flag", "fail");
			resultMap.put("result_code", response.getHeader("error_code"));
		}else if(response.getHeader("Authorization") != null){
			resultMap.put("flag", "success");
			resultMap.put("token", response.getHeader("Authorization"));
		}
		return resultMap;
	}
	
	// 토큰 검증 API
	@GetMapping("/user/tokenVerification")
	public Map<String, String> tokenVerification(HttpServletResponse response){
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("error_code") != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			resultMap.put("flag", "success");
		}
		
		return resultMap;
		
	}
	
	
	@GetMapping("/user/userinsert")
	public void userinsert() {
		
		service.insertuser();
		
	}
	
	
	// 리액트 사용안함. 20231203
//	@GetMapping("/user/main")
//	public Map<String, String> userMainRequest(HttpServletResponse response) {
//		
//		Map<String, String> resultMap = new HashMap<String, String>();
//		
//		if(response.getHeader("error_code") != null) {
//			resultMap.put("result_code", response.getHeader("error_code"));
//		}else {
//			resultMap.put("success", "200");
//		}
//		
//		return resultMap;
//	}
	
	// 리액트 사용안함. 20231203 
//	@GetMapping("/user/logout")
//	public Map<String, String> userLogoutRequest(HttpServletResponse response) {
//		
//		Map<String, String> resultMap = new HashMap<String, String>();
//		
//		if(response.getHeader("error_code") != null) {
//			resultMap.put("result_code", response.getHeader("error_code"));
//		}else {
//			resultMap.put("success", "200");
//		}
//		
//		return resultMap;
//	}'
	
	

	
}
