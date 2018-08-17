/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zcc.game.service.UserService;
import com.zcc.game.vo.UserVO;

/**
 * @author c0z00k8
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@RequestMapping("/getUsers")
	@ResponseBody
	public List<UserVO> getUsers(){
		UserVO user=new UserVO();
		user.setId(1);
		return userService.getUsers(user);
	}
	
}
