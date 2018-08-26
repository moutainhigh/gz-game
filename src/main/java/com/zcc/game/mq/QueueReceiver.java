///**
// * 版权：zcc
// * 作者：c0z00k8
// * @data 2018年8月23日
// */
//package com.zcc.game.mq;
//
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.zcc.game.service.HomeService;
//
///**
// * @author c0z00k8
// *
// */
//@Component  
//@RabbitListener(queues = "test-queue")  
//public class QueueReceiver {
//
//	@Autowired
//	private HomeService homeService;
//	
//	@RabbitHandler  
//    public void process(String message) {  
//    	System.out.println("收到rabbitmq消息："+message);
//    	homeService.updatePoolJF(message);
//    }  
//}
