package com.early.www.common.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.common.config.RabbitmqConfig;
import com.early.www.common.service.RabbitmqService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitmqServiceImpl implements RabbitmqService {

	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	RabbitmqConfig config;
	
	@Override
	public void sendMsg(String msg) {
		log.info("rabbitmq 발신 >>>>>>>>>>>>>>>>>  : {}", msg);
		rabbitTemplate.convertAndSend("fanout.exchange", "" , msg);
		log.info("rabbitmq 발신하는 pod의 queuename : {}", config.getQueueName());
		
	}

}
