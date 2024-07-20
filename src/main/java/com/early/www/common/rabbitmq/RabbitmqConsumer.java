package com.early.www.common.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.common.config.RabbitmqConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitmqConsumer {
	
	@Autowired
	RabbitmqConfig config;

//	@RabbitListener(queues = "#{queue.name}")
//	public void receiveMessage(String msg) {
//		log.info("rabbit mq 메시지 수신 영역 ! msg : {} ", msg);
//		log.info("rabbit mq 메시지 수신 영역 ! queueName : {} ", config.getQueueName());
//	}
	
}
