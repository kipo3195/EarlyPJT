package com.early.www.address.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.early.www.address.VO.AddressVO;
import com.early.www.address.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AddressController {
	
	@Autowired
	AddressService addressService;
	
	@PostMapping("/address/list")
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
	

}
