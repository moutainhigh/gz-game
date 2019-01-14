package com.zcc.game.utils.mq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.zcc.game.service.HomeService;
import com.zcc.game.service.UserService;
import com.zcc.game.utils.MD5Util;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.ChangeCenterVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.UserVO;

@Component
public class JmsUtil {

	@Autowired
	private HomeService homeService;
	@Autowired
	private UserService userService;
	@Autowired
	private JmsTemplate jmsTemplate;
	
	public void sendMsg(String queue,String message){
		jmsTemplate.convertAndSend(queue,message);
	}
	
	/**
	 * 挂卖积分
	 * @param message
	 */
	@JmsListener(destination="add_business_queue")
	public void receiveUploadQueue(String message){
		System.out.println("add_business_queue --收到消息："+message);
		BusinessVO business = new BusinessVO();
		String str[]=message.split(",");
		String userid=str[0];
		String selljf=str[1];
		String safepwd=str[2];
        business.setUserid(str[0]);
        business.setSelljf(str[1]);
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userid));
        user.setStatus("0");
        NoticeVO notice=new NoticeVO();
        notice.setAttribute1("1");
        notice.setAttribute2(userid);
        List<UserVO> users = userService.getUsers(user);
        if(users ==null || users.size()<=0){
        	notice.setTitle("添加挂卖失败");
            notice.setContent("添加挂卖失败,用户ID异常");
        	return;
        }
        double businessjf=new Double(users.get(0).getJfbusiness());
    	String pwd=users.get(0).getSafepwd();
    	if(businessjf<Integer.parseInt(selljf) || !MD5Util.MD5(safepwd).equals(pwd)){
    		notice.setTitle("添加挂卖失败");
            notice.setContent("添加挂卖失败,安全码错误或交易积分小于挂卖积分");
        	return;
    	}
		try {
			int result = homeService.addBusiness(business);
			if(result==1){
				notice.setTitle("挂卖成功");
	            notice.setContent("添加挂卖成功,您成功挂卖了"+selljf+"积分。");
			}else{
				notice.setTitle("挂卖失败");
	            notice.setContent("添加挂卖失败,系统异常请稍后重试！");
			}
			homeService.addNotices(notice);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("receiveQueue error queue=test message:"+e.getMessage());
		}
	}
	
	/**
	 * 积分转换
	 * @param message
	 */
	@JmsListener(destination="jf_transformation")
	public void receiveQueue(String message){
		System.out.println("jf_transformation--收到消息："+message);
		String msg[]=message.split(",");
		if(null==msg || msg.length!=3){
			return;
		}
		String type=msg[0];
		String jf=msg[1];
		String userId=msg[2];
		UserVO user=new UserVO();
        user.setId(Integer.parseInt(userId));
        List<UserVO> users = userService.getUsers(user);
        if(users==null || users.size()<=0 ){
        	return;
        }
        
        //比较数据version是否最新
        double zhuce = new Double(users.get(0).getJfzhuce()) -users.get(0).getJfDiya();
        double center = new Double(users.get(0).getJfcenter());
        Double business = users.get(0).getJfbusiness();
        Double jftask = users.get(0).getJftask();
        Double jfrale=new Double(jf);
        NoticeVO notice=new NoticeVO();
        notice.setAttribute1("2");//积分转换
        notice.setAttribute2(userId);
        //记录中心积分转换日志
        ChangeCenterVO chnageCenter=new ChangeCenterVO();
        if("1".equals(type) && jfrale<=zhuce){//(注册->中心)
        	user.setJfzhuce(-jfrale);
        	user.setJfcenter(jfrale);
        	chnageCenter.setUserid(userId);
        	chnageCenter.setNum(jfrale+"");
        	chnageCenter.setStatus("积分转换");
        	chnageCenter.setType("转入");
        	
        	notice.setTitle("注册积分->中心积分");
            notice.setContent(users.get(0).getAccount()+" 用户注册积分转中心积分，当前注册积分为："+zhuce+" , 中心积分为："+center);
        }else if("2".equals(type) && jfrale<=center){//(注册->交易)--需求变更为：--(中心->注册)
        	user.setJfcenter(-jfrale);
        	user.setJfzhuce(jfrale);
        	
        	notice.setTitle("中心积分->注册积分");
            notice.setContent(users.get(0).getAccount()+" 用户中心积分转注册积分，当前注册积分为："+zhuce+" , 中心积分为："+center);
        }else if("4".equals(type) && jfrale<=center){//(中心->交易)
        	user.setJfcenter(-jfrale);
        	user.setJfbusiness(jfrale);
        	chnageCenter.setUserid(userId);
        	chnageCenter.setNum(jfrale+"");
        	chnageCenter.setStatus("积分转换");
        	chnageCenter.setType("转出");
        	
        	notice.setTitle("中心积分->交易积分");
            notice.setContent(users.get(0).getAccount()+" 用户中心积分转交易积分，当前交易积分为："+business+" , 中心积分为："+center);
        }else if("5".equals(type) && jfrale<=center){//(中心->任务)
        	//要有预申请的任务积分
        	if(users.get(0).getPrejftask()>0 && users.get(0).getPrejftask().equals(new Double(jf))){
        		user.setJfcenter(-jfrale);
            	user.setJftask(jfrale);
            	user.setPrejftask(new Double(0));
        	}else{
        		notice.setTitle("积分转换失败");
                notice.setContent(users.get(0).getAccount()+" 用户中心积分转任务积分失败，预申请任务积分："+users.get(0).getPrejftask()+",转换积分："+jf);
                homeService.addNotices(notice);
                return;
        	}
        	chnageCenter.setUserid(userId);
        	chnageCenter.setNum(jfrale+"");
        	chnageCenter.setStatus("积分转换");
        	chnageCenter.setType("转出");
        	
        	notice.setTitle("中心积分->任务积分");
            notice.setContent(users.get(0).getAccount()+" 用户中心积分转任务积分，当前交易积分为："+jftask+" , 中心积分为："+center);
        }else{
        	notice.setTitle("积分转换失败");
            notice.setContent(users.get(0).getAccount()+" 用户积分转换错误,转换类型错误");
        }
        userService.updateUser(user,chnageCenter);
        homeService.addNotices(notice);//添加日志
	}
	
	/**
	 * 开奖，更新用户收益
	 * @param message
	 */
//	@JmsListener(destination="win_data")
//	public void receiveWinData(String message){
//		System.out.println("win_data--收到消息："+message);
//	}
}
