/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcc.game.mapper.UserMapper;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.ChangeCenterVO;
import com.zcc.game.vo.GiveTokenVO;
import com.zcc.game.vo.ParamVO;
import com.zcc.game.vo.ReplyVO;
import com.zcc.game.vo.UserVO;

/**
 * @author c0z00k8
 *
 */
@Service
public class UserService {

	
	@Autowired
	private UserMapper userMapper;
	
	public int addReply(ReplyVO reply){
		return userMapper.addReply(reply);
	}
	public List<UserVO> getUsers(UserVO user){
		return userMapper.getUsers(user);
	}
	public List<UserVO> getChilds(UserVO user){
		return userMapper.getChilds(user);
	}
	public int insertUser(UserVO user){
		return userMapper.insertUser(user);
	}
	public int updateUser(UserVO user){
		return userMapper.updateUser(user);
	}
	public int updateBusiness(BusinessVO user){
		return userMapper.updateBusiness(user);
	}
	
	@Transactional
	public int updateUser(UserVO user,ChangeCenterVO chnageCenter){
		//写入中心积分转换日志
		if(chnageCenter.getNum()!=null || !"".equals(chnageCenter.getNum())){
			userMapper.addChangeCenter(chnageCenter);
		}
		return userMapper.updateUser(user);
	}
	
	@Transactional
	public int gaveToken(UserVO user){
		int m=0;
		int n=0;
		UserVO child=new UserVO();
		child.setAccount(user.getAccount());
		child.setTaskToken(user.getTaskToken());
		m=userMapper.gaveToken(child);
		UserVO parent=new UserVO();
		parent.setId(user.getId());
		parent.setTaskToken(-user.getTaskToken());
		n=userMapper.gaveToken(parent);
		//添加赠送秘钥日志。
		if(m>0 && n>0){
			GiveTokenVO giveToken=new GiveTokenVO();
			giveToken.setAccount(user.getAccount());
			giveToken.setNum(user.getTaskToken()+"");
			giveToken.setPid(user.getId()+"");
			userMapper.addGiveToken(giveToken);
		}
		return 1;
	}
	public boolean isExistUser(UserVO user){
		List<UserVO> list= getUsers(user);
		if(list.size()>0){
			return true;
		}
		return false;
	}
	
	public ParamVO getParam(ParamVO param){
		return userMapper.getParam(param);
	}
	public List<GiveTokenVO> getGiveToken(GiveTokenVO param){
		return userMapper.getGiveToken(param);
	}
	public List<ChangeCenterVO> getCenterJf(ChangeCenterVO param){
		return userMapper.getCenterJf(param);
	}
}
