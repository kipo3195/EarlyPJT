package com.early.www.common.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.early.www.repository.CommonRepository;
import com.early.www.user.model.EarlyUser;

import lombok.RequiredArgsConstructor;

// 시큐리티가 Filter를 가지고있는데 그 Filter중에 BasicAuthenticationFilter라는 것이 있다.
// 권한이나 인증이 필요한 특정주소를 요청했을때 위 필터를 무조건 타게되어 있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않음. 현재 프로젝트는 모든 요청에 대해 해당 필터를 타고있음. 
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

	private CommonRepository commonRepository;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, CommonRepository commonRepository) {
		super(authenticationManager);
		
		this.commonRepository = commonRepository;
		
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
	
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨.");
		// 사실 인증, 권한이 필요한 주소 뿐아니라 모든 url 요청시 해당 메소드가 호출된다. 
		// doFilterInternal 내부 로직을 봤을때 header에 JWT 토큰이 있는지 없는지 판단하고 
		// 없는경우 (로그인 X) 다음 Chain으로 넘겨주기만 하면 될 것이고,
		// 있는경우 header의 JWT 토큰을 검증한 다음 해당 사용자의 authentication을 SecurityContextHolder이 관리할 수 있도록
		// set 한 다음 다음 Chain으로 넘겨주면 된다. 
		// 아마도 해당 사용자의 권한정보를 세션에 넣어서 사용하기 위함이라고 판단된다.
		
		String jwtHeader = null;
		if(request.getHeader("Authorization") != null) {
			jwtHeader = request.getHeader("Authorization");
			System.out.println("jwtHeader : "+ jwtHeader);
		}
		// 유효성 검사
		if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
			chain.doFilter(request, response);
			return;
		}
		
		// jwt header에 넘어온 토큰을 통해서 정상적인 사용자인지 체킹
		//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		//String jwtToken = response.getHeader("Authorization").replace("Bearer ", "");
		String jwtToken = jwtHeader.replace("Bearer ", "");
	
		String username = JWT.require(Algorithm.HMAC512("early")).build().verify(jwtToken).getClaim("username").asString();

		// 서명이 정상적
		if(username != null) {
			EarlyUser userEntity = commonRepository.findByusername(username);
			
			PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
			
			// JWT TOKEN 서명이 정상일때 Authentication 객체를 생성하는 방법.
			// 실제로 로그인을 진행하는것이 아니라 Authentication 객체를 임의로 생성하는 것임. 
			// username이 null이 아니라는 것은 정상적으로 인증이되었다는 것이므로 password자리에 null
			// 권한은 알려줘야함. principalDetails.getAuthorities()
			Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
			System.out.println("authentication : "+authentication);
			System.out.println(" principalDetails.getAuthorities() : " +  principalDetails.getAuthorities());
			// security session 공간에 강제로 접근하여 authentication 객체 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
			System.out.println("완료 ! ");
			
		}
		chain.doFilter(request, response);
	
	}


	
	
}
