package com.early.www.common.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

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
	  
	public void request(String socialLoginType) {
	    String redirectURL = null;
	    
	    switch (socialLoginType) {
	        case "google": {
	            redirectURL = googleOauth.getOauthRedirectURL();
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
	
	public void requestAccessToken(String socialLoginType, String code) {
		
		System.out.println("1");
		 switch (socialLoginType) {
	        case "google": {
	            googleOauth.requestAccessToken(code);
	        }
	        default:
	    }
		 
		 try {
			response.addHeader("Google", "login");
			response.sendRedirect("/login");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	  
}
