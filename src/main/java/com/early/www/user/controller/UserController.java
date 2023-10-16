package com.early.www.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	
	@GetMapping("/v1/user/main")
	public String main() {
		
		System.out.println("user main 호출");
		
		return "usermain";
	}
	
}
