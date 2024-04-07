package com.early.www.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.common.service.CommonService;
import com.early.www.properties.OAuthProperties;
import com.early.www.properties.TestProperties;

@RestController
public class CommonRestController {

	@Autowired
	CommonService service;
	
	@Autowired
	TestProperties testProperties;
	
	@Autowired
	OAuthProperties oAuthProperties;
	
	@GetMapping("/testRedis")
	public void testSet(@RequestParam String data) {
		
		service.testRedisSet(data);
		
	}

	@GetMapping("/testRedisGet")
	public void testGet(@RequestParam String data) {
		
		service.testRedisGet(data);
		
	}
	
	@GetMapping("/test")
	public void test() {
//		System.out.println(testProperties.getStringConfig());
//		System.out.println(testProperties.getIntConfig());
//		System.out.println(testProperties.getListConfig());
//		System.out.println(testProperties.getMapConfig());
//		System.out.println(oAuthProperties.getClientId());
//		System.out.println(oAuthProperties.getGoogleLoginUrl());
	}
	
}
