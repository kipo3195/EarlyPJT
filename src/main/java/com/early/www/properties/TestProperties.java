package com.early.www.properties;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "test")
@Data
public class TestProperties {

	private String stringConfig;
	private int intConfig;
	private Map<String, String> mapConfig;
	private List<String> listConfig;
	

}
