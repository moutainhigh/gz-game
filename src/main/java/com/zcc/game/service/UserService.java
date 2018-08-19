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
import com.zcc.game.vo.UserVO;

/**
 * @author c0z00k8
 *
 */
@Service
public class UserService {

	
	@Autowired
	private UserMapper userMapper;
	
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
	@Transactional
	public int gaveToken(UserVO user){
		UserVO child=new UserVO();
		child.setAccount(user.getAccount());
		child.setTaskToken(user.getTaskToken());
		userMapper.gaveToken(child);
		UserVO parent=new UserVO();
		parent.setId(user.getId());
		parent.setTaskToken(-user.getTaskToken());
		return userMapper.gaveToken(parent);
	}
	public boolean isExistUser(UserVO user){
		List<UserVO> list= getUsers(user);
		if(list.size()>0){
			return true;
		}
		return false;
	}
}
