package com.early.www.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CommonRequestCheck {
	
	public boolean errorCheck(HttpServletRequest request, HttpServletResponse response, Object dto) {
		boolean result = false;
		log.info("[{}] request, body : {}", request.getRequestURI(), dto);

		String error = (String) response.getHeader("error_code");
		
		if(error != null) {
			result = true;
		}
		
		return result;
	}
	

}
