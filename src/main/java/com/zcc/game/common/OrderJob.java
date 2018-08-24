package com.zcc.game.common;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.zcc.game.service.HomeService;


public class OrderJob extends QuartzJobBean{
	
	protected Logger logger = LoggerFactory.getLogger(OrderJob.class);

	@Override
	protected void executeInternal(JobExecutionContext paramJobExecutionContext)
			throws JobExecutionException {
		
		try {
			JobDetail jobDetail = paramJobExecutionContext.getJobDetail();
			JobDataMap jobDataMap = jobDetail.getJobDataMap();
			String businessid = jobDataMap.getString("businessid");
			HomeService mstationService = (HomeService) jobDataMap.get("HomeService");
			mstationService.checkBusiness(businessid);
			
			//执行完后删除定时任务
			Scheduler sched = (Scheduler) jobDataMap.get("sched");
			sched.deleteJob(jobDetail.getName(), jobDetail.getGroup());
			System.out.println("定时任务执行成功");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("定时任务执行失败:"+e.getMessage());
		}
	}
	
}
