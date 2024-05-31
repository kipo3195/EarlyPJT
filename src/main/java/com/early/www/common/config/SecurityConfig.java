package com.early.www.common.config;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.early.www.common.jwt.JwtAuthorizationFilterV2;
import com.early.www.repository.CommonRepository;
import com.early.www.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	private final CommonRepository commonRepository;
	
	private final TokenRepository tokenRepository;
	
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
		
//		// csrf를 사용하게 되면 
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.addFilter(corsFilter)
		.formLogin().disable()	// formLogin().disable 한다는 것은 spring security가 제공하는 form 로그인을 사용하지 않겠다는 것이며, api 인증 방식인
								// JwtAuthenticationFilter에 정의된 방식으로 인증처리를 진행함. 
		.addFilter(new JwtAuthenticationFilter(authenticationManager(authenticationConfiguration), tokenRepository))
		.addFilter(new JwtAuthorizationFilterV2(authenticationManager(authenticationConfiguration), commonRepository, tokenRepository))
		.requestMatchers().antMatchers("/user/**","/login","/address/**","/file/**").and() // 특정 URL에만 Filter를 태우도록 처리함. (jwt)
		.authorizeRequests()	// 다음 리퀘스트에 대한 사용자 권한 체크
		//.antMatchers("/user/**").hasRole("USER") //role 기반이 아닌 url - jwt 기반으로 처리
		.anyRequest().permitAll(); // 다른 요청은 권한 없이 들어갈 수 있도록 처리함


		
		return http.build();
	}
	
}
