package com.early.www.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.formLogin().disable()
		.httpBasic().disable()
		.authorizeRequests()	// 시큐리티 처리에 HttpServletRequest를 이용한다는 것을 의미함.
		
		.antMatchers("/v1/user/**")
		.access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
		
		
		.anyRequest().permitAll(); // 다른 요청은 권한 없이 들어갈 수 있도록 처리함 
		
		
		return http.build();
	}
	
}
