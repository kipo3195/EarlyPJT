package com.early.www.common.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.early.www.common.service.RabbitmqService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitmqServiceImpl implements RabbitmqService {

	@Autowired
	private final RabbitTemplate rabbitTemplate;
	
	@Override
	public void sendMsg(String msg) {
		log.info("rabbitmq service impl msg : {}", msg);
		rabbitTemplate.convertAndSend("fanout.exchange", "" , msg);
		
	}

}
