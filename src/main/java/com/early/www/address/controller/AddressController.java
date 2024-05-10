package com.early.www.address.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.address.DTO.AddressSearchDTO;
import com.early.www.address.DTO.AddressVO;
import com.early.www.address.model.AddressUserMapping;
import com.early.www.address.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/address")
public class AddressController {
	
	@Autowired
	AddressService addressService;
	
	// 주소록 리스트 조회
	@PostMapping("/list")
	public JSONObject getAddressList(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AddressVO addressVo){
		log.info("[{}], body : {}", request.getRequestURI(), addressVo);
		JSONObject resultJson = new JSONObject();
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			JSONObject type = new JSONObject();
			type.put("result", "fail");
			
			JSONObject data = new JSONObject();
			data.put("error_msg", response.getHeader("error_code"));
			
			resultJson.put("type", type);
			resultJson.put("data", data);
			
			log.info("[{}]", request.getRequestURI(), resultJson);
		}else {
			
			resultJson = addressService.getAddressList(addressVo.getUserId(), addressVo.getLimit());
			
		}
		
		System.out.println(resultJson);
		return resultJson;
	}
	
	// 주소록 친구 추가 검색 
	@PostMapping("/searchUser")
	public JSONObject getSearchUser(HttpServletRequest request, HttpServletResponse response, @RequestBody AddressSearchDTO addressSearchDTO){
		JSONObject resultJson = new JSONObject();
		log.info("[{}], body : {}", request.getRequestURI(), addressSearchDTO);
		String error = (String) response.getHeader("error_code");
		
		if(error != null) {
			JSONObject type = new JSONObject();
			type.put("result", "fail");
			
			JSONObject data = new JSONObject();
			data.put("error_msg", response.getHeader("error_code"));
			
			resultJson.put("type", type);
			resultJson.put("data", data);
			
			log.info("[{}]", request.getRequestURI(), resultJson);
		}else {
			resultJson = addressService.getSearchUser(addressSearchDTO);
		}
		
		return resultJson;
	}
	
	// 주소록에 신규 등록
	@PostMapping("/addUser")
	public JSONObject addUser(HttpServletRequest request, HttpServletResponse response, @RequestBody AddressUserMapping addressAddDTO){
		JSONObject resultJson = new JSONObject();
		log.info("[{}], body : {}", request.getRequestURI(), addressAddDTO);
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			JSONObject type = new JSONObject();
			type.put("result", "fail");
			
			JSONObject data = new JSONObject();
			data.put("error_msg", response.getHeader("error_code"));
			
			resultJson.put("type", type);
			resultJson.put("data", data);
			
			log.info("[{}]", request.getRequestURI(), resultJson);
		}else {
			
			resultJson = addressService.putUser(addressAddDTO);
		
		}
		
		return resultJson;
	}
	
	@GetMapping("/test")
	public void test(HttpServletRequest request, HttpServletResponse response, @RequestParam String username) {
		
		Random rand = new Random();
		double a = rand.nextDouble()*1000;
		int result = (int) a;
		System.out.println(result);
		
		ResponseCookie cookie = ResponseCookie.from("username", result+"")
				.maxAge(3600)			// 1시간
				.httpOnly(true)		// 브라우저에서 쿠키에 접근할 수 없도록 제한
				.build();
		response.setHeader("Set-Cookie", cookie.toString());
		
		
	}
	
	
	

}
