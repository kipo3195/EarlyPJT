package com.early.www.common.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOauth implements SocialOauth {
	
//    @Value("${spring.security.oauth2.client.registration.google.redirect-url}")
//    private String GOOGLE_SNS_BASE_URL;
//    @Value("${spring.security.oauth2.client.registration.google.client-id}")
//    private String GOOGLE_CLIENT_ID;

    
	@Override
	public String getOauthRedirectURL() {
		Map<String, Object> params = new HashMap<>();
        params.put("scope", "profile");
        params.put("response_type", "code");
        params.put("client_id", "917660405716-5mdk8ab542luosidsriaijp46e5d174f.apps.googleusercontent.com");
        params.put("redirect_uri", "http://localhost:8080/auth/google/callback");

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return "https://accounts.google.com/o/oauth2/v2/auth" + "?" + parameterString;
	}

	@Override
	public String requestAccessToken(String code) {
		
		RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", "917660405716-5mdk8ab542luosidsriaijp46e5d174f.apps.googleusercontent.com");
        params.put("client_secret", "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");
        params.put("redirect_uri", "http://localhost:8080/auth/google/callback");
        params.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("https://oauth2.googleapis.com/token", params, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
        	System.out.println(responseEntity.getBody());
            return responseEntity.getBody();
        }
        return "구글 로그인 요청 처리 실패";
	}

}
