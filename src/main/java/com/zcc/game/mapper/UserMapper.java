/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zcc.game.vo.UserVO;

/**
 * @author c0z00k8
 *
 */
@Component
public interface UserMapper {
	public List<UserVO> getUsers(UserVO user);
	
	
	
}
