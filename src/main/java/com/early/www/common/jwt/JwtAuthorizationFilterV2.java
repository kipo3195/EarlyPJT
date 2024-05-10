package com.early.www.common.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.early.www.repository.CommonRepository;
import com.early.www.repository.TokenRepository;
import com.early.www.user.model.EarlyUser;
import com.early.www.user.model.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// 20240120
// 기존에 있는 JwtAuthorizationFilter는 토큰 인증 방법이 아래와 같다.
// 서버로 요청하는 로그인을 제외한 url을 request할때 access token을 넘기고
// JwtAuthorizationFilter에서 access 토큰 검증을 하고 access token이 만료되었다면 다시 갱신요청을 할 수 있도록 코드를 전달하게끔 처리되어있었다.
// 그러면 서버로 요청하는 모든 기능은 access token이 만료되면 다시 refresh 토큰으로 갱신 요청 하고 갱신된 access로 요청을 하게 끔 처리해야 했었다.
// 이러한 프로세스를 분리하기 위해 JwtAuthorizationFilterV2를 사용한다.

// JwtAuthorizationFilterV2는 웹 워커를 사용하여 멀티 스레드 처리를 통해 access token을 갱신하는 로직을 분리 시키는 설계가 반영된 Filter이다.
// Get방식의 /user/tokenVerification에 대한 처리와 모든 서버의 request를 별도로 분리하여 처리하고자 한다.  

	/* 에러코드 정의 
	 * 
	 * 400 access token expired
	 * 
	 * 401 refresh token expired 
	 * 
	 * 402
	 * 
	 * 403 there isn't access token, refresh token 
	 * 
	 * */
@Slf4j
public class JwtAuthorizationFilterV2 extends BasicAuthenticationFilter {

	
	private CommonRepository commonRepository;
	private TokenRepository tokenRepository;
	
	public JwtAuthorizationFilterV2(AuthenticationManager authenticationManager, CommonRepository commonRepository, TokenRepository tokenRepository) {
		super(authenticationManager);
		
		this.commonRepository = commonRepository;
		this.tokenRepository = tokenRepository;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		
		String requestUrl = request.getRequestURI();
		
		String jwtHeader = null; 
		String username = null;
		
		// 토큰 검증이든 비즈니스 로직이든 accessToken은 체크해야함. 
		if(request.getHeader("Google") != null) {
			System.out.println("여기 !!!!");
			
			chain.doFilter(request, response);
			return;
		}
		
		if(request.getHeader("Authorization") != null) {
			// accessToken
			jwtHeader = request.getHeader("Authorization");
		
			// 유효성 검사
			if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
				log.info("[{}] Authorization data is invalid ! : {}",requestUrl, jwtHeader);
				response.addHeader("error_code", "403");
				chain.doFilter(request, response);
				return;
			}
			
			// jwt header에 넘어온 token을 통해서 정상적인 사용자인지 체킹
			String jwtToken = jwtHeader.replace("Bearer ", "");
			System.out.println();
			//System.out.println("토큰 검증 로직에서 jwtToken : " + jwtToken);
			try {
				
				 username = JWT.require(Algorithm.HMAC512("early")).build().verify(jwtToken).getClaim("username").asString();
				 //System.out.println("토큰 검증 로직에서 username : " + username);
				 
			}catch(TokenExpiredException e) {
				// access token 만료 -> 무조건 로그아웃 처리 
				log.info("[{}] TokenExpiredException !", requestUrl);
				response.addHeader("error_code", "400");
				chain.doFilter(request, response);
				return;
				
			}
			
			// 서명이 정상적
			if(username != null) {
				
				// url이 user인 경우에만 username을 Filter에서 전달 해줌.
				if(requestUrl.startsWith("/user")) {
					
					EarlyUser userEntity = commonRepository.findByusername(username);
					// userEntity 가 null 인경우 
					if(userEntity == null) {
						log.info("[{}] userEntity is null", requestUrl);
						response.addHeader("error_code", "403");
						chain.doFilter(request, response);
						return;
					}
					//System.out.println("토큰 검증로직에서 userEntity : "+userEntity);
					PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

					//System.out.println("토큰 검증로직에서 principalDetails : "+principalDetails);
					
					// web worker를 통한 access token 재발급 API
					if(request.getRequestURI().equals("/user/accessToken")) {
						String nowDate = null;
						boolean tokenFlag = false;
						try {
							// cookie(refresh 토큰) 검증
							Cookie[] cookies = request.getCookies();
							for(Cookie cookie : cookies) {
								if(cookie != null) {
									if(cookie.getName().equals("refreshToken")) {
										
										//cookie에 refresh token이 있는경우 true;
										tokenFlag = true;
										
										String refreshToken = cookie.getValue();
										try {
											// refresh token 검증 
											nowDate = JWT.require(Algorithm.HMAC512("early")).build().verify(refreshToken).getClaim("nowDate").asString();
										}catch(TokenExpiredException e2) {
											// refresh token 만료 
											log.info("[/user/accessToken] refresh token expired ! logout. user id : {}", username);
											response.addHeader("error_code", "401");
											chain.doFilter(request, response);
											return;
											
										}
										if(refreshToken != null && nowDate != null) {
											//System.out.println("토큰 검증시 refreshToken : " +refreshToken);
											//System.out.println("토큰 검증시 nowDate : " +nowDate);
											RefreshToken savedToken = tokenRepository.findByRefreshTokenAndCreateTime(refreshToken, nowDate);
											//System.out.println("토큰 검증시 savedToken : "+ savedToken);
											if(savedToken != null) {
												// token 갱신 처리

												String newAccessToken = createAccessToken(savedToken);
												response.addHeader("Authorization", "Bearer "+newAccessToken); //Bearer 한칸 띄고 jwtToken
											}
										}	
									}
								}
							}
							
						}catch(NullPointerException e2) {
							log.info("[/user/accessToken] cookies is null ! logout. user id : {}", username);
							response.addHeader("error_code", "401");
							chain.doFilter(request, response);
							return;
						}
						if(!tokenFlag) {
							log.info("[/user/accessToken] refreshToken is null ! logout. user id : {}", username);
							response.addHeader("error_code", "401");
							chain.doFilter(request, response);
						}
					}
					
					request.setAttribute("username", principalDetails.getUsername());
				}
			}else {
				// 토큰은 검증했지만 username이 없는경우?
			}
			
		}else if(SecurityContextHolder.getContext().getAuthentication() != null && request.getRequestURI().equals("/login")) {
			// 20231230 기준 SecurityContextHolder.getContext().setAuthentication 하는 로직은 로그인 성공시 뿐이다.
			// redis에서 미확인 건수 조회용 -> AuthentificationFilter에서 setAuthentication 에서 사용자 객체의 username 뽑기.
			// 사용자 id를 가져오려면 AuthentificationFilter에서 cookie에 접근 해야됨(request나 response 영역에 set)
			// 그것보다는 필터간 공유 영역인  SecurityContextHolder 영역에 접근하여 데이터를 가져오도록 처리함. 
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			PrincipalDetails user = (PrincipalDetails) authentication.getPrincipal();
			username = user.getEarlyUser().getUsername();
			log.info("[/login] user id : {} ", username);	
			request.setAttribute("username", username);
		}else {
			log.info("[{}] access token is invalid ! ", request.getRequestURI());
			response.addHeader("error_code", "403");
		}
		
		chain.doFilter(request, response);
	}
	
	private String nowDate() {
		String result = null;
		SimpleDateFormat sDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		result = sDate.format(new Date());
		return result;
	}

	private String createAccessToken(RefreshToken refreshToken) {
		String accessToken = JWT.create()
				.withSubject("accessToken") // TOKEN 이름
				.withExpiresAt(new Date(System.currentTimeMillis()+(30000))) // 만료시간 10초
				.withClaim("username", refreshToken.getUsername())
				.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함.  
		
		return accessToken;
	}
	
	private void refreshTokenDBinsert(String time, String refreshToken, String username) {
		RefreshToken token = new RefreshToken();
		token.setCreateTime(time);
		token.setRefreshToken(refreshToken);
		token.setUsername(username);
		
		tokenRepository.save(token);
		
	}

}
