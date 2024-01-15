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
import org.springframework.security.core.context.SecurityContext;
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
	
		System.out.println("[JwtAuthorizationFilter] 인증이나 권한이 필요한 주소 요청 !");
		System.out.println("[JwtAuthorizationFilter] requestUrl : "+request.getRequestURI());

		
		// 사실 인증, 권한이 필요한 주소 뿐아니라 모든 url 요청시 해당 메소드가 호출된다. 
		// doFilterInternal 내부 로직을 봤을때 header에 JWT token이 있는지 없는지 판단하고 
		// 없는경우 (로그인 X) 다음 Chain으로 넘겨주기만 하면 될 것이고,
		// 있는경우 header의 JWT token을 검증한 다음 해당 사용자의 authentication을 SecurityContextHolder이 관리할 수 있도록
		// set 한 다음 다음 Chain으로 넘겨주면 된다. 
		// 아마도 해당 사용자의 권한정보를 세션에 넣어서 사용하기 위함이라고 판단된다.
		
		String jwtHeader = null; 
		String username = null;
		
		// access token 
		if(request.getHeader("Authorization") != null) {
			jwtHeader = request.getHeader("Authorization");
			// 유효성 검사
			if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
				response.addHeader("error_code", "403");
				chain.doFilter(request, response);
				return;
			}
			// jwt header에 넘어온 token을 통해서 정상적인 사용자인지 체킹
			String jwtToken = jwtHeader.replace("Bearer ", "");
			try {
				
				 username = JWT.require(Algorithm.HMAC512("early")).build().verify(jwtToken).getClaim("username").asString();
				 
			}catch(TokenExpiredException e) {
				// access token 만료되었을때 
				String nowDate = null;
				boolean tokenFlag = false;
				if(request.getRequestURI().equals("/user/accessToken")) {
					
					try {
						// cookie 검증
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
										System.out.println("[JwtAuthorizationFilter] refresh token 만료 ! 로그아웃 처리");
										response.addHeader("error_code", "401");
										chain.doFilter(request, response);
										return;
										
									}
									if(refreshToken != null && nowDate != null) {
										RefreshToken savedToken = tokenRepository.findByRefreshTokenAndCreateTime(refreshToken, nowDate);
										if(savedToken != null) {
											// token 갱신 처리

											String newAccessToken = createAccessToken(savedToken);
											response.addHeader("Authorization", "Bearer "+newAccessToken); //Bearer 한칸 띄고 jwtToken
											System.out.println("[JwtAuthorizationFilter] token 재발급 성공 , newAccessToken : "+ newAccessToken);
											chain.doFilter(request, response);
											
											// 20231122 기존의 설계는 access token을 재발급 할때 refresh token도 갱신 처리 했는데 
											// 이렇게 되면 로그아웃 하지 않고 사용하는 사용자들이 발생할 수 있다는 생각이 들어서 주석처리함
											// 만약 refreshToken을 재발급 한다고 하더라고 해당 로직을 주석 처리하는 것이 아니라 
											// access token의 갱신 시간이 남아 있을때 검증 후 처리하는 것이 바람직해 보임. 
											// String newRefreshToken = createRefreshToken(savedToken);
											// response.setHeader("Set-Cookie", newRefreshToken);
										}
									}	
								}
							}
							
						}
						
					}catch(NullPointerException e2) {
						System.out.println("[JwtAuthorizationFilter] cookies 없음 -> 로그아웃 처리 ");
						response.addHeader("error_code", "401");
						// 다시 로그인 하는 error_code 주기 
						chain.doFilter(request, response);
						return;
					}
					if(!tokenFlag) {
						// 다시 로그인 하는 error_code 주기 
						System.out.println(" [JwtAuthorizationFilter] Cookie에 refreshToken 없음 -> 로그아웃 처리");
						response.addHeader("error_code", "401");
						chain.doFilter(request, response);
					}
				}else {
					// 다시 token을 요청할 수 있도록 response
					response.addHeader("error_code", "400");
					chain.doFilter(request, response);
				}
				return;
			}
			
			// 서명이 정상적 20231214 여기 이하 로직 정리
			// access 토큰에서 추출한 id를 DB에 검증한다면 jwt를 사용하는 이유가 없음. 해당 부분에서 SecurityContextHolder에 저장해야되는지 검증하기
			// 저장하지 않아도 된다면 DB 조회로직 제거 
			// 이후 로그인 할때 session에 넣은 데이터를 조회 해 와서 request.setAttribute("username", username); 처리 할 수 있는지 해보기
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
				
				// 20231230 아래의 로직들이 주석처리 된 이유
				// SecurityContextHolder, security에서 제공하는 Filter chain을 학습했을때 
				// SecurityContextHolder영역은 ThreadLocal이기 때문에 하나의 요청에 대하여 Filter chain 간에서만 동일한 Authentication에 접근 가능하다.
				// 즉 현재의 로직상에서는 사용하지 않는다. 
				
				// JWT TOKEN 서명이 정상일때 Authentication 객체를 생성하는 이유?
				// JwtAuthentication처럼 실제로 사용자 정보에 기반한 Authentication 객체를 생성하는 것이 아니라 임의의 Authentication를 생성하는 것임. 
				// username이 null이 아니라는 것은 정상적으로 인증이되었다는 것이므로 password 자리에 null, 권한은 알려줘야함. principalDetails.getAuthorities()
				// 이하 로직을 실행시키는 것이 임의의 Authentication를 만드는 것.
				// Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
				
				// System.out.println(" principalDetails.getAuthorities() : " +  principalDetails.getAuthorities());
				
				// security session 공간에 접근하여 authentication 객체 저장
				// SecurityContextHolder.getContext().setAuthentication(authentication);
				
				request.setAttribute("username", username);
			}
		
		
		}else if(SecurityContextHolder.getContext().getAuthentication() != null && request.getRequestURI().equals("/login")) {
			// 20231230 기준 SecurityContextHolder.getContext().setAuthentication 하는 로직은 로그인 성공시 뿐이다.
			System.out.println("[JwtAuthorizationFilter] 로그인 요청 ");		
			
			// redis에서 미확인 건수 조회용 -> AuthentificationFilter에서 setAuthentication 에서 사용자 객체의 username 뽑기.
			// 사용자 id를 가져오려면 AuthentificationFilter에서 cookie에 접근 해야됨(request나 response 영역에 set)
			// 그것보다는 필터간 공유 영역인  SecurityContextHolder 영역에 접근하여 데이터를 가져오도록 처리함. 
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			PrincipalDetails user = (PrincipalDetails) authentication.getPrincipal();
			username = user.getEarlyUser().getUsername();
			System.out.println("SecurityContextHolder : " + username);
			request.setAttribute("username", username);
		}else {
			System.out.println("[JwtAuthorizationFilter] access token 없음");
			response.addHeader("error_code", "403");
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
