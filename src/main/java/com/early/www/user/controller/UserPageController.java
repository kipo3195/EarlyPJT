package com.early.www.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserPageController {

	
	@GetMapping("/user/mainPage")
	public ModelAndView userMainRequest() {
		
		ModelAndView mov = new ModelAndView("/user/usermainPage");
		
		return mov;
		
		
	}
	
}
