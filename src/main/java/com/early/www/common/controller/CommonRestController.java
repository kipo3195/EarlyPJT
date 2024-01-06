package com.early.www.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.common.service.CommonService;

@RestController
public class CommonRestController {

	@Autowired
	CommonService service;
	
	@GetMapping("/testRedis")
	public void testSet(@RequestParam String data) {
		
		service.testRedisSet(data);
		
	}

	@GetMapping("/testRedisGet")
	public void testGet(@RequestParam String data) {
		
		service.testRedisGet(data);
		
	}
	
}
