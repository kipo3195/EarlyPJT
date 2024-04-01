package com.early.www.common.oauth;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.early.www.repository.TokenRepository;
import com.early.www.user.model.EarlyUser;
import com.early.www.user.model.RefreshToken;


@Component
public class GoogleOauth implements SocialOauth {
	
	@Autowired
	CommonService service;
	
	@Autowired
	private TokenRepository tokenRepository;
	
	@Override
	public String getOauthRedirectURL(String clientType) {
		Map<String, Object> params = new HashMap<>();
        params.put("scope", "profile");
        params.put("response_type", "code");
        params.put("client_id", "917660405716-5mdk8ab542luosidsriaijp46e5d174f.apps.googleusercontent.com");
        params.put("redirect_uri", "http://localhost:8080/auth/google/callback/"+clientType);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return "https://accounts.google.com/o/oauth2/v2/auth" + "?" + parameterString;
	}

	@Override
	public String requestAccessToken(String code, String clientType) {
		
		RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", "917660405716-5mdk8ab542luosidsriaijp46e5d174f.apps.googleusercontent.com");
        params.put("client_secret", "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");
        params.put("redirect_uri", "http://localhost:8080/auth/google/callback/"+clientType);
        params.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("https://oauth2.googleapis.com/token", params, String.class);

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
        
		System.out.println();
		System.out.println(resultJson.get("id"));
		
		String userId = (String) resultJson.get("id");
		String name = (String) resultJson.get("name");
		EarlyUser user = new EarlyUser();
		
		user.setUsername(socialLoginType+"_"+userId);
		user.setName(name);
		user.setProvider(socialLoginType);
		user.setPassword(socialLoginType+"_"+userId);
		
		// 회원가입 및 로그인 가능 사용자  
		boolean result = service.userJoinOAuth(user);
		
		if(result) {
			// 토큰 생성
			String accessToken = JWT.create()
					.withSubject("accessToken") // TOKEN 이름
					.withExpiresAt(new Date(System.currentTimeMillis()+(30000))) // 만료시간 10초
					//.withClaim("id", principalDetails.getEarlyUser().getId())
					.withClaim("username", userId)
					.sign(Algorithm.HMAC512("early"));  // 서버만 아는 고유한 값이어야함.
			
			response.addHeader("Authorization", "Bearer "+accessToken); //Bearer 한칸 띄고 jwtToken
			
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
			
			refreshTokenDBinsert(nowDate, refreshToken, userId);
			
			// error 처리 redirect
			switch(clientType) {
			
			case "web":
				try {
					response.sendRedirect("http://localhost:3000/login");
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
				break;
			case "ios":
				break;
			case "aos":
				break;
			
			}
			
		}
		
        return "";
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
