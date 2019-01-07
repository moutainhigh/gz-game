/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

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
@Component
public interface UserMapper {
	
	public List<UserVO> getUsers(UserVO user);
	public List<UserVO> getChilds(UserVO user);
	public int insertUser(UserVO user);
	public int updateUser(UserVO user);
	public int updateBusiness(BusinessVO user);
	public int gaveToken(UserVO user);
	

	public ParamVO getParam(ParamVO param);
	public int addGiveToken(GiveTokenVO giveToken);
	public List<GiveTokenVO> getGiveToken(GiveTokenVO giveToken);
	
	public int addChangeCenter(ChangeCenterVO changeCenterVO);
	public List<ChangeCenterVO> getCenterJf(ChangeCenterVO center);
	
	
	public int addReply(ReplyVO reply);
}
