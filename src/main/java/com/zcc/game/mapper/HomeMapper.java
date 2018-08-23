/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.DataVO;
import com.zcc.game.vo.MessageVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.PoolVO;
import com.zcc.game.vo.TaskVO;

/**
 * @author c0z00k8
 *
 */
@Component
public interface HomeMapper {
	
	public List<NoticeVO> getNotices(NoticeVO user);
	public List<BusinessVO> getBusiness(BusinessVO business);
	public int addBusiness(BusinessVO business);
	public int updateBusiness(BusinessVO business);
	
	public int addTask(TaskVO task);
	public int updateTask(TaskVO task);
	public List<TaskVO> getTask(TaskVO task);
	
	public List<DataVO> getData(DataVO task);
	public int addData(DataVO task);
	
	public int addPool(PoolVO pool);
	public List<PoolVO> getPools(PoolVO pool);
	public int updatePools(List<PoolVO> pools);
	
	//添加留言
	public int addMessage(MessageVO message);
	public List<MessageVO> getMessages(MessageVO message);
	
	
}
