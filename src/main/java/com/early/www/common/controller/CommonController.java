package com.early.www.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public ModelAndView main() {
		
		ModelAndView mov = new ModelAndView("/main"); 
		
		return mov; 
		
	}
	
	
	/************************** 
	 * 
	 * 회원가입 & 로그인 관련 처리 
	 *
	 ************************** */

	@GetMapping("/login")
	public ModelAndView login() {

		ModelAndView mov = new ModelAndView("/login"); 
		
		return mov;
	}
	
	@PostMapping("/loginRequest")
	public String loginRequest(@RequestBody EarlyUser user) {
		
		
		System.out.println("/loginRequest 호출 !");
		
		return "/main";
	}
	
	
	@GetMapping("join")
	public ModelAndView join() {
		
		ModelAndView mov = new ModelAndView("/join");
		
		return mov;
	}
	
	@PostMapping("/joinRequest")
	public String joinRequest(EarlyUser user) {
		
		service.userJoin(user);
		
		return "redirect:/";
	}
	
	
}
