package com.early.www.common.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.early.www.common.oauth.GoogleOauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthService {
	
	private final GoogleOauth googleOauth;
	 
	private final HttpServletResponse response;
	  
	public void request(String socialLoginType, String clientType) {
	    String redirectURL = null;
	    
	    switch (socialLoginType) {
	        case "google": {
	            redirectURL = googleOauth.getOauthRedirectURL(clientType);
	        } break;
		default:
			break;
	    }
	    
	    try {
			response.sendRedirect(redirectURL);
		} catch (IOException e) {
			log.debug(e.toString());
		}
	}
	
	public String requestAccessToken(String socialLoginType, String code, String clientType) {
		
		String result = null;
		 switch (socialLoginType) {
	        case "google": {
	           result = googleOauth.requestAccessToken(code, clientType);
	        }
	        default:
	     }
		 
		 return result;
	}
	
	
	public void getUserInfo(String socialLoginType, String token, String clientType) {
		
		 switch (socialLoginType) {
	        case "google": {
	        	googleOauth.getUserInfo("https://www.googleapis.com/oauth2/v2/userinfo", socialLoginType, token, clientType, response);
	        }
	        default:
	     }
		
		
	}
	  

	public void response() {
		try {
			response.sendRedirect("localhost:3000/");
			//TODO header 값을 주고 감지 할 수 있는지 여부 체크
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	
}
