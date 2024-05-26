package com.early.www.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "deployment")
@Data
public class DeploymentProperties {

	private String type;
}
