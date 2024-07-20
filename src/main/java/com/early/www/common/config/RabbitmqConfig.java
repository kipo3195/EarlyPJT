package com.early.www.common.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Base64UrlNamingStrategy;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.NamingStrategy;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.early.www.properties.RabbitmqProperties;

@Configuration
public class RabbitmqConfig {

	@Value("${spring.rabbitmq.host}")
	private String host;
	
	@Value("${spring.rabbitmq.username}")
	private String username;
	
	@Value("${spring.rabbitmq.password}")
	private String password;
	
	@Value("${spring.rabbitmq.port}")
	private int port;
	
	private String queueName;
	
	public String getQueueName() {
		return queueName;
	}

	// AnonymousQueue 생성시 queue naming prefix
	@Autowired
	RabbitmqProperties properties; 
	
	// Exchange 생성
	@Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout.exchange");
    }
	
	// 익명 chat Queue 생성
	@Bean
	public Queue chatQueue() {
		
		NamingStrategy namingStrategy = new NamingStrategy() {
			@Override
			public String generateName() {
				Base64UrlNamingStrategy strategy = new Base64UrlNamingStrategy(properties.getChatQueueNamingStrategy());
				queueName = strategy.generateName();
				return queueName;
			}
		};
	
		return new AnonymousQueue(namingStrategy); 
	}
	
	// Exchange와 Queue 연결
	@Bean
    Binding binding(FanoutExchange fanoutExchange, Queue chatQueue) {
        return BindingBuilder.bind(chatQueue).to(fanoutExchange); // fanout이라 전체에 보냄. 만약 to(DirectExchage)인 경우 with("키") 메소드 필요
    }
	
	// rabbitmq와 연결을 위한 ConnectionFactory 생성
	@Bean
	ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
	
	// messageConverter 생성 - producer - customer간 데이터를 주고 받기 위한 규약을 json으로 처리함. 
	@Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

	// 실제 메시지를 발신하기 위한 rabbitmq template. convertAndSend()를 통해 메시지 발송처리. 
	@Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
	
	
	
}
