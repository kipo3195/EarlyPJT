package com.early.www.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.early.www.common.service.CommonService;
import com.early.www.user.model.EarlyUser;

@Controller
public class CommonController {
	
	@Autowired
	CommonService service;
	
	/***************************
	 * 
	 * 메인화면 및 홈 화면 호출 
	 *
	 *************************** */
	
	@GetMapping("/")
	public ModelAndView main(HttpServletRequest request,HttpServletResponse response) {
		
		ModelAndView mov = new ModelAndView("/main"); 
		
		System.out.println(request.getHeader("Authorization"));
		
		return mov; 
		
	}
	
	/************************** 
	 * 
	 * 회원가입 & 로그인 관련 처리 
	 *
	 ************************** */

	// 로그인 페이지 요청
	@GetMapping("/login")
	public ModelAndView login() {

		ModelAndView mov = new ModelAndView("/login"); 
		
		return mov;
	}
	
	
	// 회원가입 페이지 요청
	@GetMapping("join")
	public ModelAndView join() {
		
		ModelAndView mov = new ModelAndView("/join");
		
		return mov;
	}
	
	// 회원가입 요청 
	@PostMapping("/joinRequest")
	public String joinRequest(EarlyUser user) {
		
		service.userJoin(user);
		
		return "redirect:/";
	}
	
	// ajax
	// 회원가입 전 ID 중복 체크
	@GetMapping("/idDupCheck")
	@ResponseBody
	public String idDupCheck(@RequestParam String username) {
		
		System.out.println("idDupCheck id : " + username);
		
		boolean result = service.existsUsername(username);

		System.out.println("CommonController idDupCheck result : "+ result);
		
		if(result) {
			return "true";
		}else {
			return "false";
		}
	}
	
	@GetMapping("/user/main")
	public ModelAndView userMain() {
		
		System.out.println("usermain page 호출 !");
		ModelAndView mov = new ModelAndView("/user/usermain");
		
		return mov;
	}
	
}
