package com.early.www.address.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.address.DTO.AddressSearchDTO;
import com.early.www.address.DTO.AddressVO;
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
	public Map<String, Object> getAddressList(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AddressVO addressVo){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			
			resultMap = addressService.getAddressList(addressVo.getUserId(), addressVo.getLimit());
			
		}
		
		return resultMap;
	}
	
	// 주소록 친구 추가 검색 
	@PostMapping("/searchUser")
	public Map<String, Object> getSearchUser(HttpServletRequest request, HttpServletResponse response, @RequestBody AddressSearchDTO addressSearchDTO){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String error = (String) response.getHeader("error_code");
		if(error != null) {
			resultMap.put("flag", "fail");
			resultMap.put("error_code", response.getHeader("error_code"));
		}else {
			resultMap = addressService.getSearchUser(addressSearchDTO);
		}
		
		return resultMap;
	}
	

}
