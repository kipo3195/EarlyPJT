package com.early.www.common.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.early.www.repository.TokenRepository;
import com.early.www.user.model.EarlyUser;
import com.early.www.user.model.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;

// This filter by default responds to the URL /login.
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
			
			System.out.println("[JwtAuthenticationFilter] 로그인 시도 요청");
			
			// 1.username, password 받기
			try {
//				BufferedReader br = request.getReader();
//				//Stream 안에 username, password가 있음.
//				
//				String input = null;
//				
//				while((input = br.readLine()) != null) {
//					System.out.println(input);
//				}
			
				//json 형태로 파싱함
				ObjectMapper om = new ObjectMapper();
				System.out.println(om);
				EarlyUser earlyUser = om.readValue(request.getInputStream(), EarlyUser.class);
				
				// 토큰생성 
				UsernamePasswordAuthenticationToken authenticationToken 
						= new UsernamePasswordAuthenticationToken(earlyUser.getUsername(), earlyUser.getPassword());
				// PrincipalDetailsService의 loadUserByUsername가 실행 = 실제 로그인 시도해서 만드는 Authentication
				// DB에 있는 username, password가 일치 authentication != null
				
				Authentication authentication = authenticationManager.authenticate(authenticationToken);
				// 계정 정보가 존재하지 않는다면 여기서 throws exception 
				
				if(authentication == null) {
					System.out.println("[JwtAuthenticationFilter] 일치하는 정보가 없습니다. ");
				}else {
					// authentication에는 principalDetails (사용자 정보 객체), Credentials, Authenticated(T/F), Granted Authorities 정보 등이 담겨있음.
					// System.out.println("[JwtAuthenticationFilter] authentication : " +authentication);
					
					// 로그인된 사용자의 계정명을 출력하는 로그 
					// PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
					// System.out.println("[JwtAuthenticationFilter] username : " + principalDetails.getEarlyUser().getUsername());
					
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
			System.out.println("[JwtAuthenticationFilter] successfulAuthentication 인증완료 ");
			
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
					.withExpiresAt(new Date(System.currentTimeMillis()+(360000))) // 만료시간 1시간
					.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함. 
			
			ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
					.maxAge(3600)			// 1시간
					.httpOnly(true)		// 브라우저에서 쿠키에 접근할 수 없도록 제한
					.build();
			response.setHeader("Set-Cookie", cookie.toString());
			// refresh token create end 

			
			// refresh token DB insert 
			refreshTokenDBinsert(nowDate, refreshToken, principalDetails.getUsername());

			// 해당 토큰을 header - Authorization에 담아서 return ..
			// 다음번 요청시 해당 TOKEN으로 접근시 유효한지 체크하면 됨.(Filter)
			// 원래 쿠키 + 세션으로 체크하는 것을 JWT TOKEN으로 처리하는 것임.
			
			// SecurityContextHolder 영역에 저장.
			SecurityContextHolder.getContext().setAuthentication(authResult);
			
			chain.doFilter(request, response);
		}

		@Override
		protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException failed ) throws IOException, ServletException {
			super.unsuccessfulAuthentication(request, response, failed);
			System.out.println("[JwtAuthenticationFilter] unsuccessfulAuthentication 계정 정보 존재하지 않음 ");
			
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
