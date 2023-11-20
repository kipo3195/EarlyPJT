package com.early.www.common.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

// 시큐리티가 Filter를 가지고있는데 그 Filter중에 BasicAuthenticationFilter라는 것이 있다.
// 권한이나 인증이 필요한 특정주소를 요청했을때 위 필터를 무조건 타게되어 있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않음. 현재 프로젝트는 모든 요청에 대해 해당 필터를 타고있음. 
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

	private CommonRepository commonRepository;
	private TokenRepository tokenRepository;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, CommonRepository commonRepository, TokenRepository tokenRepository) {
		super(authenticationManager);
		
		this.commonRepository = commonRepository;
		this.tokenRepository = tokenRepository;
	}
	

	// 인증이나 권한이 필요한 주소요청이 있을때 해당 필터를 타게됨. 
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// super.doFilterInternal(request, response, chain); 
		// doFilterInternal에서는 request를 convert하여 UsernamePasswordAuthenticationToken를 생성하는데
		// 해당 시점에는 UsernamePasswordAuthenticationToken의 인스턴스가 null
		// 그렇기 때문에 chain.doFilter(request, response) 해버려서 security session에 set하지 못함.  
		// so, 주석 처리 
	
		System.out.println("[JwtAuthorizationFilter] 인증이나 권한이 필요한 주소 요청이 됨.");
		// 사실 인증, 권한이 필요한 주소 뿐아니라 모든 url 요청시 해당 메소드가 호출된다. 
		// doFilterInternal 내부 로직을 봤을때 header에 JWT 토큰이 있는지 없는지 판단하고 
		// 없는경우 (로그인 X) 다음 Chain으로 넘겨주기만 하면 될 것이고,
		// 있는경우 header의 JWT 토큰을 검증한 다음 해당 사용자의 authentication을 SecurityContextHolder이 관리할 수 있도록
		// set 한 다음 다음 Chain으로 넘겨주면 된다. 
		// 아마도 해당 사용자의 권한정보를 세션에 넣어서 사용하기 위함이라고 판단된다.
		
		
		String jwtHeader = null; 
		// access token 
		if(request.getHeader("Authorization") != null) {
			jwtHeader = request.getHeader("Authorization");
			// 유효성 검사
			if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
				response.addHeader("error_code", "403");
				chain.doFilter(request, response);
				return;
			}
			// jwt header에 넘어온 토큰을 통해서 정상적인 사용자인지 체킹
			String jwtToken = jwtHeader.replace("Bearer ", "");
			String username = null;
			
			try {
				System.out.println("jwtToken : "+ jwtToken);
				 username = JWT.require(Algorithm.HMAC512("early")).build().verify(jwtToken).getClaim("username").asString();
				 System.out.println("11111111111111");
				 System.out.println("request uri " + request.getRequestURI());
				 // jwtToken : undefined 
			}catch(TokenExpiredException e) {
				// 토큰이 만료되었을때 
				if(request.getRequestURI().equals("/user/accessToken")) {
					// http only cookie
					Cookie[] cookies = request.getCookies();
					boolean tokenFlag = false;
					try {
						for(Cookie cookie : cookies) {
							if(cookie != null) {
								if(cookie.getName().equals("refreshToken")) {
									tokenFlag = true;
									String refreshToken = cookie.getValue();
									String nowDate = JWT.require(Algorithm.HMAC512("early")).build().verify(refreshToken).getClaim("nowDate").asString();
									if(refreshToken != null && nowDate != null) {
										RefreshToken savedToken = tokenRepository.findByRefreshTokenAndCreateTime(refreshToken, nowDate);
										if(savedToken != null) {
											// 토큰 갱신 처리
											String newAccessToken = createAccessToken(savedToken);
											String newRefreshToken = createRefreshToken(savedToken);
											
											response.addHeader("Authorization", "Bearer "+newAccessToken); //Bearer 한칸 띄고 jwtToken
											response.setHeader("Set-Cookie", newRefreshToken);
											
											chain.doFilter(request, response);
										}
									}	
								}
							}
						}
					}catch(NullPointerException e2) {
						System.out.println("http only cookies 없음 -> 로그아웃 처리 ");
						// 다시 로그인 하는 error_code 주기 
						return;
					}
					if(!tokenFlag) {
						// 다시 로그인 하는 error_code 주기 
						System.out.println("cookies는 있는데 refreshToken Cookie 없음 -> 로그아웃 처리");
					}
				}else {
					// 다시 토큰을 요청할 수 있도록 response
					response.addHeader("error_code", "400");
					chain.doFilter(request, response);
				}
				return;
			}
			
			
			// 서명이 정상적
			if(username != null) {
				
				// header에 따른 분기처리
				String type = request.getHeader("type");
				if(type != null) {
					// 로그아웃
					if(type.equals("logout")) {
						System.out.println("[JwtAuthorizationFilter] logout request username " + username);
					}
				}
				
				EarlyUser userEntity = commonRepository.findByusername(username);
				
				// userEntity 가 null 인경우 
				if(userEntity == null) {
					response.addHeader("error_code", "403");
					chain.doFilter(request, response);
					return;
				}
				
				PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
				
				// JWT TOKEN 서명이 정상일때 Authentication 객체를 생성하는 방법.
				// 실제로 로그인을 진행하는것이 아니라 Authentication 객체를 임의로 생성하는 것임. 
				// username이 null이 아니라는 것은 정상적으로 인증이되었다는 것이므로 password자리에 null
				// 권한은 알려줘야함. principalDetails.getAuthorities()
				Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
				System.out.println(" principalDetails.getAuthorities() : " +  principalDetails.getAuthorities());
				// security session 공간에 강제로 접근하여 authentication 객체 저장
				
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		
		
		}

		chain.doFilter(request, response);
	}


	private String createRefreshToken(RefreshToken refreshToken) {
		String nowDate = nowDate();
		String newRefreshToken = JWT.create()
				.withSubject("refreshToken") // TOKEN 이름
				.withClaim("nowDate", nowDate)
				.withExpiresAt(new Date(System.currentTimeMillis()+(60000))) // 만료시간 1분
				.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함. 
		
		ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
				.maxAge(60)			// 1분 
				.httpOnly(true)		// 브라우저에서 쿠키에 접근할 수 없도록 제한
				.build();
		
		// refresh token DB insert 
		refreshTokenDBinsert(nowDate, newRefreshToken, refreshToken.getUsername());
		
		return cookie.toString();
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
				.withExpiresAt(new Date(System.currentTimeMillis()+(10000))) // 만료시간 10초
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
