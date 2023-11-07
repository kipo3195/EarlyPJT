package com.early.www.user.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.common.service.CommonService;

@RestController
public class UserController {

	// react axios 테스트
	@GetMapping("/api/test")
	public Map<String, String> test() {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("data", "success");
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
	
	@Autowired
	CommonService service;
	// 로그인 요청
	@PostMapping("/login")
	@ResponseBody
	public Map<String, String> loginRequest(String username, String password, String token, HttpServletResponse response) {
		
		System.out.println("[UserController] loginRequest ");
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("Authorization") != null) {
			//System.out.println("[UserController] Authorization : " + response.getHeader("Authorization"));
			resultMap.put("flag", "success");
			resultMap.put("token", response.getHeader("Authorization"));
		}else {
			resultMap.put("flag", "fail");
			
		}
		return resultMap;
		
	}
	
	
	@GetMapping("/user/main")
	public Map<String, String> userMainRequest(HttpServletResponse response) {
		
		System.out.println("[UserController] userMainRequest ");
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("error_code") != null) {
			resultMap.put("result_code", response.getHeader("error_code"));
		}else {
			resultMap.put("success", "200");
		}
		
		return resultMap;
	}
	
	@GetMapping("/user/logout")
	public Map<String, String> userLogoutRequest(HttpServletResponse response) {
		
		System.out.println("[UserController] userLogoutRequest ");
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("error_code") != null) {
			resultMap.put("result_code", response.getHeader("error_code"));
		}else {
			resultMap.put("success", "200");
		}
		
		return resultMap;
	}
	
	
}
