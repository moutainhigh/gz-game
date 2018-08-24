package com.zcc.game.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcc.game.common.OrderJob;
import com.zcc.game.common.QuartzJobUtils;
import com.zcc.game.mapper.HomeMapper;
import com.zcc.game.mapper.UserMapper;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.DataVO;
import com.zcc.game.vo.MessageVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.PoolVO;
import com.zcc.game.vo.TaskVO;
import com.zcc.game.vo.TokenVO;
import com.zcc.game.vo.UserVO;

@Service
public class HomeService {

	@Autowired
	private HomeMapper homeMapper;
	@Autowired
	private UserMapper userMapper;
	
	/**
     * 获取定时任务执行时间
     * @param fixTime
     * @return
     */
    private String getCronExpressionByFixTime(int num) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.SECOND, num);
    	Date date = calendar.getTime();
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
    	
        return sdf.format(date);
    }
    
	public List<NoticeVO> getNotices(NoticeVO notice){
		return homeMapper.getNotices(notice);
	}
	public List<BusinessVO> getBusiness(BusinessVO business){
		return homeMapper.getBusiness(business);
	}
	public int addBusiness(BusinessVO business) throws Exception{
		int num = homeMapper.addBusiness(business);
		return num;
	}
	public int addMessage(MessageVO message){
		return homeMapper.addMessage(message);
	}
	public List<MessageVO> getMessages(MessageVO message){
		return homeMapper.getMessages(message);
	}
	public int addToken(TokenVO token){
		return homeMapper.addToken(token);
	}
	public List<TokenVO> getTokens(TokenVO token){
		return homeMapper.getTokens(token);
	}
	//回调，检查交易数据,未打款，封号，1，待售，2，交易中，3，已完成。4，过期
	@Transactional
	public void checkBusiness(String id){
		BusinessVO business=new BusinessVO();
		business.setId(Integer.parseInt(id));
		List<BusinessVO> bs=homeMapper.getBusiness(business);
		String status = bs.get(0).getStatus();
		String userid = bs.get(0).getUserid();
		if("2".equals(status)){//待交易，封号，创建新记录交易。
			business.setStatus("4");//返回待交易状态
			homeMapper.updateBusiness(business);
			UserVO user=new UserVO();
			user.setId(Integer.parseInt(userid));
			user.setStatus("1");//无效账号
			userMapper.updateUser(user);
			BusinessVO newBs=new BusinessVO();
			newBs.setUserid(bs.get(0).getUserid());
			newBs.setSelljf(bs.get(0).getSelljf());
			homeMapper.addBusiness(newBs);
		}
	}
	
	@Transactional
	public int updateBusiness(BusinessVO business) throws Exception{
		if("4".equals(business.getStatus())){//过期未付款，从新生成挂卖信息，封号买家
//			BusinessVO addbusiness = new BusinessVO();
//			addbusiness.setUserid(business.getUserid());
//			addbusiness.setSelljf(business.getSelljf());
//	        homeMapper.addBusiness(addbusiness);
//	        UserVO user=new UserVO();
//	        user.setId(Integer.parseInt(business.getBuyerid()));
//	        user.setStatus("1");//封号
//	        userMapper.updateUser(user);
		}else if("3".equals(business.getStatus())){//卖家确认收款，积分转换，交易完成
			BusinessVO tempVO=new BusinessVO();
			tempVO.setId(business.getId());
			List<BusinessVO> list = homeMapper.getBusiness(business);
			String sellJf=list.get(0).getSelljf();
			int jf=Integer.parseInt(sellJf)*100;
			String buyerId=list.get(0).getBuyerid();
			String sellerId=list.get(0).getUserid();
			UserVO user=new UserVO();
			user.setId(Integer.parseInt(buyerId));
			user.setJfcenter(jf);
			userMapper.updateUser(user);//购买人增加积分
			user.setId(Integer.parseInt(sellerId));
			user.setJfbusiness(-jf);
			user.setPretake(-jf);
			userMapper.updateUser(user);//卖出人减去积分
		}else if("2".equals(business.getStatus())){//买家购买，状态变为交易中
			//添加定时任务，24小时之内付款，否则封号，解压状态继续售卖。
			// 添加定时任务
	     	JobDataMap jobDataMap = new JobDataMap();
	        jobDataMap.put("businessid", business.getId()+"");
	        jobDataMap.put("HomeService", this);
	        
	        //获取后台参数
	        QuartzJobUtils.addJob("CANCEL_ORDER_"+business.getId(), OrderJob.class, getCronExpressionByFixTime(60), jobDataMap);
		}
		return homeMapper.updateBusiness(business);
	}
	
	//任务信息
	public int addTask(TaskVO task){
		return homeMapper.addTask(task);
	}
	public int updateTask(TaskVO task){
		return homeMapper.updateTask(task);
	}
	public List<TaskVO> getTask(TaskVO task){
		return homeMapper.getTask(task);
	}
	
	//开奖信息
	public int addData(DataVO task){
		return homeMapper.addData(task);
	}
	public List<DataVO> getData(DataVO data){
		return homeMapper.getData(data);
	}
	@Transactional
	public int addPool(PoolVO pool){
		return homeMapper.addPool(pool);
	}
	public List<PoolVO> getPools(PoolVO pool){
		return homeMapper.getPools(pool);
	}
	
	//更新中奖用户-更新用户中奖状态，更新中奖用户积分
	public void updatePoolJF(String gmnum){
		PoolVO pool=new PoolVO();
		pool.setGmnum(gmnum);//开奖期号，如：20180818001
		DataVO data=new DataVO();
		data.setGmnum(gmnum);
		List<DataVO> datas=homeMapper.getData(data);
		data=datas.get(0);
		List<PoolVO> pools = homeMapper.getPools(pool);
		if(pools.size()>0){
			for (PoolVO p:pools) {
				int num=isWinPrize(p.getBuyinfo(), data);
				if(num>0){
					p.setStatus("1");//中奖
				}else{
					p.setStatus("2");//未中奖
//					p.setWinjf("0");
//					p.setGetjf("0");
				}
				p.setLastup_date(new Date());
			}
			homeMapper.updatePools(pools);
		}
	}
	
	public int isWinPrize(String buyInfo,DataVO data){
		int result=0;
		if("big1".equals(buyInfo) && "大".equals(data.getBgm1())){
			result=1;
		}else if("small1".equals(buyInfo) && "小".equals(data.getBgm1())){
			result=1;
		}else if("single1".equals(buyInfo) && "单".equals(data.getSgm1())){
			result=1;
		}else if("doub1".equals(buyInfo) && "双".equals(data.getSgm1())){
			result=1;
		}else if("big2".equals(buyInfo) && "大".equals(data.getBgm2())){
			result=1;
		}else if("small2".equals(buyInfo) && "小".equals(data.getBgm2())){
			result=1;
		}else if("single2".equals(buyInfo) && "单".equals(data.getSgm2())){
			result=1;
		}else if("doub2".equals(buyInfo) && "双".equals(data.getSgm2())){
			result=1;
		}else if("big3".equals(buyInfo) && "大".equals(data.getBgm3())){
			result=1;
		}else if("small3".equals(buyInfo) && "小".equals(data.getBgm3())){
			result=1;
		}else if("single3".equals(buyInfo) && "单".equals(data.getSgm3())){
			result=1;
		}else if("doub3".equals(buyInfo) && "双".equals(data.getSgm3())){
			result=1;
		}else if("big4".equals(buyInfo) && "大".equals(data.getBgm4())){
			result=1;
		}else if("small4".equals(buyInfo) && "小".equals(data.getBgm4())){
			result=1;
		}else if("single4".equals(buyInfo) && "单".equals(data.getSgm4())){
			result=1;
		}else if("doub4".equals(buyInfo) && "双".equals(data.getSgm4())){
			result=1;
		}else if("big5".equals(buyInfo) && "大".equals(data.getBgm5())){
			result=1;
		}else if("small5".equals(buyInfo) && "小".equals(data.getBgm5())){
			result=1;
		}else if("single5".equals(buyInfo) && "单".equals(data.getSgm5())){
			result=1;
		}else if("doub5".equals(buyInfo) && "双".equals(data.getSgm5())){
			result=1;
		}else if("sumbig".equals(buyInfo) && "大".equals(data.getBgmsum())){
			result=1;
		}else if("sumsmall".equals(buyInfo) && "小".equals(data.getBgmsum())){
			result=1;
		}else if("sumsingle".equals(buyInfo) && "单".equals(data.getSgmsum())){
			result=1;
		}else if("sumdoub".equals(buyInfo) && "双".equals(data.getSgmsum())){
			result=1;
		}else if("long".equals(buyInfo) && Integer.parseInt(data.getGm1())>Integer.parseInt(data.getGm5())){
			result=1;
		}else if("hu".equals(buyInfo) && Integer.parseInt(data.getGm1())<=Integer.parseInt(data.getGm5())){
			result=1;
		}
		return result;
	}
	
}
