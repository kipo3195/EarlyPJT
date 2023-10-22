package com.early.www.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

import com.early.www.common.jwt.JwtAuthenticationFilter;
import com.early.www.common.jwt.JwtAuthorizationFilter;
import com.early.www.repository.CommonRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	private final CommonRepository commonRepository;
	
	private final AuthenticationConfiguration authenticationConfiguration;
	
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		// csrf를 사용하게 되면 
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.addFilter(corsFilter)
		.formLogin().disable()
		.addFilter(new JwtAuthenticationFilter(authenticationManager(authenticationConfiguration)))
		.addFilter(new JwtAuthorizationFilter(authenticationManager(authenticationConfiguration), commonRepository))
		
		.authorizeRequests()	// 시큐리티 처리에 HttpServletRequest를 이용한다는 것을 의미함.
		
		.antMatchers("/user/**")
		.access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
		
		.anyRequest().permitAll(); // 다른 요청은 권한 없이 들어갈 수 있도록 처리함 
		
		return http.build();
	}
	
}
