package com.zcc.game.utils.mq;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfig {

	@Bean
	public Queue queue(){
		return new ActiveMQQueue("add_business_queue");
	}
}
