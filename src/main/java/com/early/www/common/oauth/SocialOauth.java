package com.early.www.common.oauth;

public interface SocialOauth {
	
	String getOauthRedirectURL();
	
	String requestAccessToken(String code);
	
}
