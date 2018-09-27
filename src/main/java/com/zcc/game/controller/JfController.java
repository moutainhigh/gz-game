package com.zcc.game.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zcc.game.common.SysCode;
import com.zcc.game.service.UserService;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.ParamVO;
import com.zcc.game.vo.UserVO;

@Controller
@RequestMapping("/jf")
public class JfController extends BaseController{

	@Autowired
	private UserService userService;
	
	//积分转换
	@RequestMapping("/transforjf")
//	@ResponseBody
	public void transforjf(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userId","type","jf","version"};
        Map<String, String> params = parseParams(request, "updateUser", paramKey);
        
        String userId = params.get("userId"); 
        String type = params.get("type"); //1(注册->中心),2(注册->交易),3(注册->任务),4(中心->交易),5(中心->任务)
        String jf = params.get("jf"); 
//        String version = params.get("version"); 
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(type) 
        		||StringUtils.isBlank(jf) ){//userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userId));
        List<UserVO> users = userService.getUsers(user);
        if(users==null || users.size()<=0 ){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        //比较数据version是否最新
        int zhuce = users.get(0).getJfzhuce()-users.get(0).getJfDiya();
        int center = users.get(0).getJfcenter();
        int jfrale=Integer.parseInt(jf)*100;
        if("1".equals(type) && jfrale<=zhuce){//(注册->中心)
        	user.setJfzhuce(-jfrale);
        	user.setJfcenter(jfrale);
        }else if("2".equals(type) && jfrale<=zhuce){//(注册->交易)
        	user.setJfzhuce(-jfrale);
        	user.setJfbusiness(jfrale);
        }else if("3".equals(type) && jfrale<=zhuce){//(注册->任务)
        	//要有预申请的任务积分
        	if(users.get(0).getVersion()>0 && users.get(0).getVersion()==Integer.parseInt(jf)){
        		user.setJfzhuce(-jfrale);
            	user.setJftask(jfrale);
            	user.setVersion(0);
        	}else{
        		renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
            	return;
        	}
        }else if("4".equals(type) && jfrale<=center){//(中心->交易)
        	user.setJfcenter(-jfrale);
        	user.setJfbusiness(jfrale);
        }else if("5".equals(type) && jfrale<=center){//(中心->任务)
        	//要有预申请的任务积分
        	if(users.get(0).getVersion()>0 && users.get(0).getVersion()==Integer.parseInt(jf)){
        		user.setJfcenter(-jfrale);
            	user.setJftask(jfrale);
            	user.setVersion(0);
        	}else{
        		renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
            	return;
        	}
        }else{
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, "积分不足或参数错误");
        	return;
        }
//        user.setVersion(Integer.parseInt(version)+1);
        int result =0;
        try {
	        //更新
	    	result = userService.updateUser(user);
	    	if(result==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SYS_ERR, "更新失败");
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````updateUser()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	//获取系统参数
	@RequestMapping("/getJf")
	public void getJf(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"number"};
        Map<String, String> params = parseParams(request, "getJf", paramKey);
        
        String number = params.get("number"); //001 注册积分,002 抵押比例,003 赔率,004 任务积分,005 返还积分
        if(StringUtils.isBlank(number)){//number不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        ParamVO param=new ParamVO();
        param.setNumber(number);
        
        try {
	        //获取前五条公告数据
	    	ParamVO result = userService.getParam(param);
	    	if(result !=null){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getNotice()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
}
