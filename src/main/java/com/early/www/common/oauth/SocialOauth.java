package com.early.www.common.oauth;

import javax.servlet.http.HttpServletResponse;

public interface SocialOauth {
	
	String getOauthRedirectURL(String clientType);
	
	String requestAccessToken(String code, String clientType);
	
	String getUserInfo(String url, String socialLoginType, String token, String clientType, HttpServletResponse response);
}
