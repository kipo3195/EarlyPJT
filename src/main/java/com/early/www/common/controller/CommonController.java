package com.early.www.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import com.early.www.user.model.EarlyUser;

@Controller
public class CommonController {
	
	
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
		System.out.println(user.getId());
		System.out.println(user.getPassword());
		System.out.println(user.getRoles());
		System.out.println(user.getUsername());
		
		return "rediect:/";
	}
	
	
	
	@GetMapping("join")
	public ModelAndView join() {
		
		ModelAndView mov = new ModelAndView("/join");
		
		return mov;
	}
	
	@PostMapping("/joinRequest")
	public String joinRequest(@RequestBody EarlyUser user) {
		
		
		System.out.println("/joinRequest 호출 !");
		System.out.println(user.getId());
		System.out.println(user.getPassword());
		System.out.println(user.getRoles());
		System.out.println(user.getUsername());
		
		return "rediect:/";
	}
	
	
}
