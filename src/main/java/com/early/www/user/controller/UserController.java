package com.early.www.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.early.www.common.service.CommonService;

@RestController
public class UserController {

	@Autowired
	CommonService service;
	// 로그인 요청
	@PostMapping("/login")
	@ResponseBody
	public Map<String, String> loginRequest(String username, String password, String token, HttpServletResponse response) {
		
		System.out.println("[UserController] 여기 호출 ");
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(response.getHeader("Authorization") != null) {
			System.out.println("[UserController] Authorization : " + response.getHeader("Authorization"));
			resultMap.put("flag", "success");
			resultMap.put("token", response.getHeader("Authorization"));
		}else {
			resultMap.put("flag", "fail");
			
		}
		return resultMap;
		
	}
	
	
	
}
