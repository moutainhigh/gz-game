package com.zcc.game.service;

import java.util.List;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcc.game.mapper.HomeMapper;
import com.zcc.game.mapper.UserMapper;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.DataVO;
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
	public int addPool(PoolVO pool){
		return homeMapper.addPool(pool);
	}
}
