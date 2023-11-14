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
		
		
		return mov; 
		
	}
	
	/************************** 
	 * 
	 * 회원가입 & 로그인 관련 처리 
	 *
	 ************************** */

	// 로그인 페이지 요청
	@GetMapping("/loginPage")
	public ModelAndView login() {

		ModelAndView mov = new ModelAndView("/loginPage"); 
		
		return mov;
	}
	
	
	// 회원가입 페이지 요청
	@GetMapping("/joinPage")
	public ModelAndView join() {
		
		ModelAndView mov = new ModelAndView("/joinPage");
		
		return mov;
	}
	
	// 회원가입 요청 html 버전 
	@PostMapping("/joinRequest")
	public String joinRequest(EarlyUser user) {
		
		service.userJoin(user);
		
		return "redirect:/";
	}
	

	
	
}
