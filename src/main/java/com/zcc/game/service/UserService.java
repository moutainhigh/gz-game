/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
}
