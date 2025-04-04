package com.early.www.common.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.early.www.properties.DeploymentProperties;
import com.early.www.properties.RedisProperties;

@Configuration
public class RedisConfig {
	
	@Value("${spring.redis.host}")
	private String host;
	
	@Value("${spring.redis.port}")
	private int port;
	
	@Autowired
	RedisProperties redisProperties;
	
	@Autowired
	DeploymentProperties deploymentProperties;
	
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
	

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		// application.yaml 에서 설정한 host, port 사용하기 
		LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(host, port);
		
		if(deploymentProperties.getType().equals("server")) {
			lettuceConnectionFactory.setPassword(redisProperties.getRedisPassword());
		}else {
		}
		return lettuceConnectionFactory;
	}
	
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(){
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		
		// lettuce Connection 설정을 위한 처리 
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		
		// 기본 직렬화가 JdkSerializationRedisSerializer.java -> byte
		// 직렬화(java -> redis) 시 byte로 저장되는 것을 String으로 인코딩 처리 되도록 하는 설정
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		
		// value를 String 형태로 저장할 수 있도록 
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		// json
		// redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer());
		
		
		return redisTemplate;
		
	}
	
}
