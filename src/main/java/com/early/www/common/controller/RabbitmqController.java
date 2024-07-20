package com.early.www.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.early.www.common.service.RabbitmqService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RabbitmqController {
	
	private final RabbitmqService rabbitmqService;
	
	@GetMapping(value= "/rabbit/test/{msg}")
	public void testRabbit(@PathVariable String msg) {
		
		rabbitmqService.sendMsg(msg);
		
	}
	
	

}
