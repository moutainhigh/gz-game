/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月30日
 */
package com.zcc.game.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zcc.game.service.HomeService;

/**
 * @author c0z00k8
 *
 */
@Component
@EnableScheduling
public class ScheduledTasks {

	@Autowired
	private HomeService homeService;
	
	
//	@Scheduled(cron = "0/10 * * * * ?")  //cron接受cron表达式，根据cron表达式确定定时规则
    public void testCron() {
        System.out.println("time:"+new Date());
        homeService.taskData();
    }

	
}
