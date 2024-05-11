package com.early.www.common.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.early.www.repository.TokenRepository;
import com.early.www.user.model.EarlyUser;
import com.early.www.user.model.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// This filter by default responds to the URL /login.
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	
	AuthenticationManager authenticationManager;
	
	private TokenRepository tokenRepository;
	
	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
		this.authenticationManager = authenticationManager;
		this.tokenRepository = tokenRepository;
	} 
	
		// 1번 실행
		@Override
		public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
				throws AuthenticationException {
			
			// 1.username, password 받기
			try {
//				BufferedReader br = request.getReader();
//				//Stream 안에 username, password가 있음.
//				
//				String input = null;
//				
//				while((input = br.readLine()) != null) {
//				}
			
				//json 형태로 파싱함
				ObjectMapper om = new ObjectMapper();
				EarlyUser earlyUser = om.readValue(request.getInputStream(), EarlyUser.class);
				
				log.info("[/login] login request user id : {}, login provider : {}", earlyUser.getUsername(), earlyUser.getProvider());

				
				// OAuth 로그인 계정 
				if(!earlyUser.getProvider().toLowerCase().equals("default")) {
					
					try {
						
						String tempPw = JWT.require(Algorithm.HMAC512("early")).build().verify(earlyUser.getPassword()).getClaim("tempPw").asString();
					
						if(tempPw != null) {
							earlyUser.setPassword(earlyUser.getUsername()+"early");
							
						} else {
							log.info("[/user/accessToken] OAuth login tempPw is null ! user id : {}", earlyUser.getUsername());
							response.addHeader("error_code", "401");
							return null;
						}

					}catch(TokenExpiredException e2) {
						// refresh token 만료 
						log.info("[/user/accessToken] OAuth login tempPw expired ! user id : {}", earlyUser.getUsername());
						response.addHeader("error_code", "401");
						return null;
					}
				}
				
				// 토큰생성 
				UsernamePasswordAuthenticationToken authenticationToken 
						= new UsernamePasswordAuthenticationToken(earlyUser.getUsername(), earlyUser.getPassword());
				// PrincipalDetailsService의 loadUserByUsername가 실행 = 실제 로그인 시도해서 만드는 Authentication
				// DB에 있는 username, password가 일치 authentication != null
				
				Authentication authentication = authenticationManager.authenticate(authenticationToken);
				// 계정 정보가 존재하지 않는다면 여기서 throws exception 
				
				if(authentication == null) {
					log.info("[/login] There are no matching users : {}", earlyUser.getUsername());
				}else {
					// authentication에는 principalDetails (사용자 정보 객체), Credentials, Authenticated(T/F), Granted Authorities 정보 등이 담겨있음.
					
					// 로그인된 사용자의 계정명을 출력하는 로그 
					// PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
					
					return authentication;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return null;
		}
		
		@Override
		protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
				Authentication authResult) throws IOException, ServletException {
			
			PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
			
			// 토큰 생성 방식
//			String jwtToken = JWT.create()
//					.withSubject("early") // TOKEN 이름
//					.withExpiresAt(new Date(System.currentTimeMillis()+(10000))) // 만료시간
//					.withClaim("id", principalDetails.getEarlyUser().getId())
//					.withClaim("username", principalDetails.getUsername())
//					.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함. 
		
			
			// access token create start
			String accessToken = JWT.create()
					.withSubject("accessToken") // TOKEN 이름
					.withExpiresAt(new Date(System.currentTimeMillis()+(30000))) // 만료시간 10초
					//.withClaim("id", principalDetails.getEarlyUser().getId())
					.withClaim("username", principalDetails.getUsername())
					.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함. 

			response.addHeader("Authorization", "Bearer "+accessToken); //Bearer 한칸 띄고 jwtToken
			// access token create end
			
			
			// refresh token create start
			String nowDate = nowDate();
			String refreshToken = JWT.create()
					.withSubject("refreshToken") // TOKEN 이름
					.withClaim("nowDate", nowDate)
					.withExpiresAt(new Date(System.currentTimeMillis()+(3600000))) // 만료시간 1시간
					.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함. 
			
			ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
					.maxAge(3600)			// 1시간
					.httpOnly(true)		// 브라우저에서 쿠키에 접근할 수 없도록 제한
					.build();
			response.setHeader("Set-Cookie", cookie.toString());
			
			// refresh token create end 
			
			//System.out.println("####### principalDetails.getUsername() "+ principalDetails.getUsername()+" refreshToken : "+ cookie.toString());
			
			// refresh token DB insert 
			refreshTokenDBinsert(nowDate, refreshToken, principalDetails.getUsername());

			// SecurityContextHolder 영역에 저장.
			SecurityContextHolder.getContext().setAuthentication(authResult);
			
			chain.doFilter(request, response);
		}

		@Override
		protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException failed ) throws IOException, ServletException {
			super.unsuccessfulAuthentication(request, response, failed);
			// 일치하는 사용자 없을때 
//			response.setStatus(HttpStatus.UNAUTHORIZED.value());
//	        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//	        Map<String, Object> body = new LinkedHashMap<>();
//	        body.put("code", HttpStatus.UNAUTHORIZED.value());
//	        body.put("error", failed.getMessage());
//
//	        new ObjectMapper().writeValue(response.getOutputStream(), body);
		}
	
		
		private void refreshTokenDBinsert(String time, String refreshToken, String username) {
			RefreshToken token = new RefreshToken();
			token.setCreateTime(time);
			token.setRefreshToken(refreshToken);
			token.setUsername(username);
			
			tokenRepository.save(token);
			
		}
		
		private String nowDate() {
			String result = null;
			SimpleDateFormat sDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			result = sDate.format(new Date());
			return result;
		}


}
