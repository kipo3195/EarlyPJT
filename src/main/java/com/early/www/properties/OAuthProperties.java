package com.early.www.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "google-oauth")
@Data
public class OAuthProperties {

	private String clientId;
	private String clientSecret;
	private String redirectUrl;
	private String tokenUrl;
	private String userInfoUrl;
	private String googleLoginUrl;
	
}
