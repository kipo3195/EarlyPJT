package com.early.www.common.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	@Bean
	public CorsFilter corsFilter() {
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true); // 내 서버가 응답을 할때 json을 자바스크립트에서 처리 할 수 있게 할지 설정(ajax.. )
		//config.addAllowedOrigin("*"); // 모든 ip의 응답 허용
		config.addAllowedOriginPattern("*"); //setAllowCredentials 를 true로 사용할때 addAllowedOrigin 대신 addAllowedOriginPattern를 사용하길 권장함. 
		config.addAllowedHeader("*"); // 모든 header의 응답 허용
		config.addAllowedMethod("*"); // 모든 http method의 응답 허용  
		source.registerCorsConfiguration("/**", config); // react localhost:3000에서 접근시
		// accesss to XMLHttpRequest at 'http://localhost:8080/login' from origin 
		// 'http://localhost:3000' has been blocked by CORS policy: Response to preflight 
		//request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present on the requested resource.
		// registerCorsConfiguration에 해당하는 경로에 대해서만 접근 허용으로 보여서 기존 /user/**에서 수정함.  
		
		return new CorsFilter(source);
	}
	
	
}