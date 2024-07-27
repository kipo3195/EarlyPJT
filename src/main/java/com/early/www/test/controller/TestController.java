package com.early.www.test.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.test.Model.TestDTO;
import com.early.www.test.service.TestService;

@RestController
public class TestController {

	
	@Autowired
	TestService testService;
	

	@PostMapping("/test/jpa")
	public void getJpa(HttpServletRequest request, HttpServletResponse response, @RequestBody TestDTO testDto) {
		
		testService.test(testDto);
		
	}
	
}
