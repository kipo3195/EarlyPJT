package com.early.www.common.oauth;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.early.www.common.service.CommonService;
import com.early.www.properties.OAuthProperties;
import com.early.www.user.model.EarlyUser;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class GoogleOauth implements SocialOauth {
	
	@Autowired
	CommonService service;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	OAuthProperties oAuthProperties;
	
	@Override
	public String getOauthRedirectURL(String clientType) {
		Map<String, Object> params = new HashMap<>();
        params.put("scope", "profile");
        params.put("response_type", "code");
        params.put("client_id", oAuthProperties.getClientId());
        params.put("redirect_uri", oAuthProperties.getRedirectUrl()+clientType);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return oAuthProperties.getGoogleLoginUrl() + "?" + parameterString;
	}

	@Override
	public String requestAccessToken(String code, String clientType) {
		
		RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", oAuthProperties.getClientId());
        params.put("client_secret", oAuthProperties.getClientSecret());
        params.put("redirect_uri", oAuthProperties.getRedirectUrl()+clientType);
        params.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(oAuthProperties.getTokenUrl(), params, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
	}
	
	@Override
	public String getUserInfo(String url, String socialLoginType, String json, String clientType, HttpServletResponse response) {
		RestTemplate restTemplate = new RestTemplate();
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = null;
		try {
			jsonObj = (JSONObject) parser.parse(json);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jsonObj.get("access_token"));
        HttpEntity entity = new HttpEntity(headers);
        JSONObject resultJson = restTemplate.exchange(url, HttpMethod.GET, entity, JSONObject.class).getBody();
		
		String userId = socialLoginType+"_"+(String) resultJson.get("id");
		String name = (String) resultJson.get("name");
		EarlyUser user = new EarlyUser();
		
		
		// 회원가입 및 로그인 가능 사용자  
		boolean result = service.userJoinOAuth(userId, name, socialLoginType);
		
		if(result) {
			
			
			// 쿠키 저장 flag
			ResponseCookie flagCookie = ResponseCookie.from("flag", "success")
					.maxAge(20)			// 20초
					.httpOnly(false)	// 브라우저에서 쿠키에 접근할 수 있도록 허용
					.build();
			response.addHeader("Set-Cookie", flagCookie.toString());
			
			// 쿠키 저장 id
			ResponseCookie idCookie = ResponseCookie.from("userId", userId)
					.maxAge(20)			// 20초
					.httpOnly(false)	// 브라우저에서 쿠키에 접근할 수 있도록 허용
					.build();
			response.addHeader("Set-Cookie", idCookie.toString());
			
			// 쿠키 저장 pw
			ResponseCookie tempCookie = ResponseCookie.from("temp", createTempToken(userId)) //임시 비밀번호. /login으로 요청시 복호화 함 
					.maxAge(20)			// 20초
					.httpOnly(false)	// 브라우저에서 쿠키에 접근할 수 있도록 허용
					.build();
			response.addHeader("Set-Cookie", tempCookie.toString());

			// 쿠키 저장 provider
			ResponseCookie providerCookie = ResponseCookie.from("provider", "google")
					.maxAge(20)			// 20초
					.httpOnly(false)	// 브라우저에서 쿠키에 접근할 수 있도록 허용
					.build();
			response.addHeader("Set-Cookie", providerCookie.toString());
			
			switch(clientType) {
			
			case "web":
				try {
					response.sendRedirect("http://localhost:3000/auth/google/callback");
					log.info("userId : {} Google OAuth redirect success ! ", userId);
					// 현재 getUserInfo에서 생성한 JWT 정보들을 cookie로 전달한다.
					// react의 Application에서 쿠키를 확인했을때 Path가 /auth/google/callback로 나온다(서버의 마지막 redirect 경로)
					// sendRedirect의 정보를 쿠키에 접근할 수 있는 url로 리다이렉트한다. 
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "ios":
				break;
			case "aos":
				break;
			
			}
			
		}else {
			// error 처리 redirect
			switch(clientType) {
			
			case "web":
				try {
					response.sendRedirect("http://localhost:3000/auth/google/error");
					log.info("userId : {} Google OAuth redirect error ! ", userId);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "ios":
				break;
			case "aos":
				break;
			
			}
			
		}
		
        return "";
	}
	
	private String createTempToken(String id) {
		String accessToken = JWT.create()
				.withSubject("tempPw") // TOKEN 이름
				.withExpiresAt(new Date(System.currentTimeMillis()+(30000))) // 만료시간 30초
				.withClaim("tempPw", id)
				.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함.  
		
		return accessToken;
	}
	

}
