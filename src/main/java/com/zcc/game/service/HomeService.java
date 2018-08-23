package com.zcc.game.service;

import java.util.Date;
import java.util.List;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.tools.javac.jvm.Pool;
import com.zcc.game.mapper.HomeMapper;
import com.zcc.game.mapper.UserMapper;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.DataVO;
import com.zcc.game.vo.MessageVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.PoolVO;
import com.zcc.game.vo.TaskVO;
import com.zcc.game.vo.UserVO;

@Service
public class HomeService {

	@Autowired
	private HomeMapper homeMapper;
	@Autowired
	private UserMapper userMapper;
	
	public List<NoticeVO> getNotices(NoticeVO notice){
		return homeMapper.getNotices(notice);
	}
	public List<BusinessVO> getBusiness(BusinessVO business){
		return homeMapper.getBusiness(business);
	}
	public int addBusiness(BusinessVO business){
		return homeMapper.addBusiness(business);
	}
	public int addMessage(MessageVO message){
		return homeMapper.addMessage(message);
	}
	public List<MessageVO> getMessages(MessageVO message){
		return homeMapper.getMessages(message);
	}
	
	
	@Transactional
	public int updateBusiness(BusinessVO business){
		if("4".equals(business.getStatus())){//过期未付款，从新生成挂卖信息，封号买家
			BusinessVO addbusiness = new BusinessVO();
			addbusiness.setUserid(business.getUserid());
			addbusiness.setSelljf(business.getSelljf());
	        homeMapper.addBusiness(addbusiness);
	        UserVO user=new UserVO();
	        user.setId(Integer.parseInt(business.getBuyerid()));
	        user.setStatus("1");//封号
	        userMapper.updateUser(user);
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
			userMapper.updateUser(user);//卖出人减去积分
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
