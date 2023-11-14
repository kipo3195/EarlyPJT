package com.early.www.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.common.service.CommonService;
import com.early.www.user.model.EarlyUser;

@RestController
public class UserController {
	
	@Autowired
	CommonService service;

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
	
	
	// 회원가입 전 ID 중복 체크
	@GetMapping("/idDupCheck")
	@ResponseBody
	public String idDupCheck(@RequestParam String username) {
		
		System.out.println("idDupCheck id : " + username);
		
		boolean result = service.existsUsername(username);

		System.out.println("[UserController] idDupCheck result : "+ result);
		
		if(result) {
			return "true";
		}else {
			return "false";
		}
	}
	
	// 회원가입 요청
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
		
		return resultMap;
	}
	
	
}
