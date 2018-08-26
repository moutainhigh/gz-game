/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.zcc.game.common.CommonUtil;
import com.zcc.game.common.SysCode;
import com.zcc.game.common.UploadType;
import com.zcc.game.service.ImageService;
import com.zcc.game.service.UserService;
import com.zcc.game.utils.MD5Util;
import com.zcc.game.vo.UserVO;

/**
 * @author c0z00k8
 *
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController{

	@Autowired
	private UserService userService;
	
	@Autowired
	private ImageService imageService;
	
	
	@RequestMapping("/getUsers")
//	@ResponseBody
	public void getUsers(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"userId"};
		Map<String, String> params = parseParams(request, "getUser", paramKey);
        String userId = params.get("userId");
        if(StringUtils.isEmpty(userId)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userId));
        try {
			List<UserVO> users = userService.getUsers(user);
//			 Map<Class<?>, String[]> includes=CommonUtil.getObjectIncludes(UserVO.class,
//		        		new String[]{"i","userTelephone","userName","userSex","userEmail","userFish",
//		        	"userGold","gameType","gameLeave","userLeave","nameChangeDate","emailChangeDate","userToken","userHeadIcm","shopId","shopName"});
		    	
	        if(users.size()>0){
	        	renderJson(request, response, SysCode.SUCCESS, users.get(0));
	        }else{
	        	renderJson(request, response, SysCode.SUCCESS, users);
	        }
		} catch (Exception e) {
			logger.error("获取用户信息失败"+e.getMessage(), e);
			renderJson(request, response, SysCode.SYS_ERR, "获取用户信息失败");
		}
	}
	@RequestMapping("/getChilds")
//	@ResponseBody
	public void getChilds(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"userId"};
		Map<String, String> params = parseParams(request, "getUser", paramKey);
        String userId = params.get("userId");
        if(StringUtils.isEmpty(userId)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userId));
        try {
			List<UserVO> users = userService.getChilds(user);
//			 Map<Class<?>, String[]> includes=CommonUtil.getObjectIncludes(UserVO.class,
//		        		new String[]{"i","userTelephone","userName","userSex","userEmail","userFish",
//		        	"userGold","gameType","gameLeave","userLeave","nameChangeDate","emailChangeDate","userToken","userHeadIcm","shopId","shopName"});
		    	
	        if(users.size()>0){
	        	renderJson(request, response, SysCode.SUCCESS, users);
	        }else{
	        	renderJson(request, response, SysCode.SUCCESS, users);
	        }
		} catch (Exception e) {
			logger.error("获取用户信息失败"+e.getMessage(), e);
			renderJson(request, response, SysCode.SYS_ERR, "获取用户信息失败");
		}
	}
	@RequestMapping("/login")
//	@ResponseBody
	public void login(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"account","password"};
		Map<String, String> params = parseParams(request, "login", paramKey);
        String account = params.get("account");
        String password = params.get("password"); 
        if(StringUtils.isEmpty(account) || StringUtils.isEmpty(password) ){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
    	
        UserVO user=new UserVO();
        user.setAccount(account);
//        user.setPassword(MD5Util.MD5(password));
        try {
        	//1,校验账户密码
			List<UserVO> users = userService.getUsers(user);
			if(users==null||users.size()==0){
				renderJson(request, response, SysCode.USER_AND_PWD_ERROR, null);//账号或密码错误
				return;
			}
			if(users.get(0).getPassword().equals(MD5Util.MD5(password))){
				renderJson(request, response, SysCode.SUCCESS, users.get(0));
			}else{
				renderJson(request, response, SysCode.SYS_ERR, "用户登陆失败");
			}
		} catch (Exception e) {
			logger.error("用户登陆失败:"+e.getMessage(), e);
			renderJson(request, response, SysCode.SYS_ERR, "用户登陆失败");
		}
	}
	
	@RequestMapping("/register")
//	@ResponseBody
	public void register(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"account","password","safepwd","jfzhuce","pid"};
        Map<String, String> params = parseParams(request, "register", paramKey);
        String account = params.get("account");
        String password = params.get("password"); 
        String safepwd = params.get("safepwd"); 
        String jfzhuce = params.get("jfzhuce"); 
        String pid = params.get("pid"); 
    	
        if(StringUtils.isEmpty(account) ||StringUtils.isEmpty(password) || 
        		StringUtils.isEmpty(safepwd)||StringUtils.isEmpty(jfzhuce) ||StringUtils.isEmpty(pid)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        UserVO userVO = new UserVO();
        userVO.setAccount(account);
        //检查该手机号是否已经注册过
      	try {
			if(userService.isExistUser(userVO)){
				renderJson(request, response, SysCode.USER_IS_REGISTE, null);//用户名已注册
				return;
			}
			userVO.setPassword(MD5Util.MD5(password));
			userVO.setSafepwd(MD5Util.MD5(safepwd));
			userVO.setJfzhuce(Integer.parseInt(jfzhuce)*100);
			userVO.setJfold(jfzhuce);
			userVO.setJfDiya(Integer.parseInt(jfzhuce)*50);
			userVO.setPid(Integer.parseInt(pid));
			
	        //注册
	    	int result = userService.insertUser(userVO);
	    	renderJson(request, response, SysCode.SUCCESS, result);
		} catch (Exception e) {
			logger.info("`````method``````register()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, CommonUtil.SQLERROR);
		}
	}
	
	@RequestMapping("/updateUser")
//	@ResponseBody
	public void updateUser(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userVO"};
        Map<String, String> params = parseParams(request, "updateUser", paramKey);
        
        String userVO = params.get("userVO"); 
        UserVO user=JSON.parseObject(userVO, UserVO.class);
        String userId=user.getId()+"";
        if(StringUtils.isBlank(userId)){//shopId,userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        if(null != user.getPassword() && !"".equals(user.getPassword())){//修改密码
        	user.setPassword(MD5Util.MD5(user.getPassword()));
        }
        if(null != user.getSafepwd() && !"".equals(user.getSafepwd())){//修改安全码
        	user.setSafepwd(MD5Util.MD5(user.getSafepwd()));
        }
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
	
	@RequestMapping("/fogetPwd")
//	@ResponseBody
	public void fogetPwd(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"id","password","newpassword","safepwd","newsafepwd"};
        Map<String, String> params = parseParams(request, "fogetPwd", paramKey);
        
        String id = params.get("id"); 
        String password = params.get("password"); 
        String newpassword = params.get("newpassword"); 
        String safepwd = params.get("safepwd"); 
        String newsafepwd = params.get("newsafepwd"); 
        if(StringUtils.isBlank(id)){//
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        if(StringUtils.isBlank(password) && StringUtils.isBlank(safepwd)){//
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(id));
        List<UserVO> users = userService.getUsers(user);
        if(StringUtils.isNotBlank(password) && StringUtils.isNotBlank(newpassword)){//修改密码
        	if(users==null || users.size()<1 || !MD5Util.MD5(password).equals(users.get(0).getPassword())){
        		renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
            	return;
        	}
        	user.setPassword(MD5Util.MD5(newpassword));
        }
        if(StringUtils.isNotBlank(safepwd) && StringUtils.isNotBlank(newsafepwd)){//修改安全码
        	if(users==null || users.size()<1 || !MD5Util.MD5(safepwd).equals(users.get(0).getSafepwd())){
        		renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
            	return;
        	}
        	user.setSafepwd(MD5Util.MD5(newsafepwd));
        }
        
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
        	logger.info("`````method``````fogetPwd()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	//赠送秘钥
	@RequestMapping("/gaveToken")
	public void gaveToken(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid","account","num","safepwd"};
        Map<String, String> params = parseParams(request, "gaveToken", paramKey);
        
        String userid = params.get("userid"); 
        String account = params.get("account"); 
        String num = params.get("num"); 
        String safepwd = params.get("safepwd"); 
        if(StringUtils.isBlank(userid) || StringUtils.isBlank(account) ||
        		StringUtils.isBlank(num) ||StringUtils.isBlank(safepwd)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        UserVO user =new UserVO();
        user.setId(Integer.parseInt(userid));
        List<UserVO> list = userService.getUsers(user);
        if(list== null || list.size()<1){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        int tokennum = list.get(0).getTaskToken();
        String safe=list.get(0).getSafepwd();
        if(!safe.equals(MD5Util.MD5(safepwd)) || tokennum<Integer.parseInt(num)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        user.setId(null);
        user.setAccount(account);
        List<UserVO> childlist = userService.getUsers(user);
        if(childlist== null || childlist.size()<1){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        user.setId(Integer.parseInt(userid));
        user.setTaskToken(Integer.parseInt(num));
        int result =0;
        try {
	        //更新
	    	result = userService.gaveToken(user);
	    	if(result==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SYS_ERR, "更新失败");
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````gaveToken()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	/**
	 * 上传——图片信息
	 * @param request
	 * @param response
	 * @throws FileUploadException 
	 * @throws IOException 
	 */
	@RequestMapping("updateImage")
	public void updateImage(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String[] paramKey = {"userId","type"};
        Map<String, String> params = parseParams(request, "updateImage", paramKey);
        String userId = params.get("userId");
        String type = params.get("type");//ali,weixin
        if(StringUtils.isEmpty(userId)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        //1 头像
//        try {
			//上传图片并获取路径
        	List<String> imageUrls = imageService.upload(request, UploadType.USER_PIC);
        	if(imageUrls==null || imageUrls.size()==0){
        		renderJson(request, response,  SysCode.SYS_ERR, null);
        		return;
        	}
        	
        	UserVO userVO=new UserVO();
            userVO.setId(Integer.parseInt(userId));
            if("ali".equals(type)){//支付宝
            	userVO.setAlipaypic(imageUrls.get(0));
            }else{//微信收款码
            	userVO.setWeixinpic(imageUrls.get(0));
            }
    		
    		//更新——完善信息
            int	result = userService.updateUser(userVO);
            renderJson(request, response, SysCode.SUCCESS, imageUrls.get(0));
//		} catch (Exception e) {
//			logger.error("上传用户图像失败:"+e.getMessage(), e);
//			renderJson(request, response, SysCode.SYS_ERR, "上传用户图像失败:");
//		}
	}
	
	
}
