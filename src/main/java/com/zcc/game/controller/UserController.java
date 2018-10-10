/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月17日
 */
package com.zcc.game.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.zcc.game.common.CommonUtil;
import com.zcc.game.common.SysCode;
import com.zcc.game.common.UploadType;
import com.zcc.game.service.ImageService;
import com.zcc.game.service.UserService;
import com.zcc.game.utils.MD5Util;
import com.zcc.game.vo.GiveTokenVO;
import com.zcc.game.vo.ParamVO;
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
        user.setStatus("0");
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
			ParamVO param=new ParamVO();
			param.setNumber("002");//注册积分抵押
			param = userService.getParam(param);
			int num=0;
			if(param.getData()!=null){
				num=Integer.parseInt(param.getData());
			}
			//判断父类积分是否够扣减
			UserVO parent = new UserVO();
			parent.setId(Integer.parseInt(pid));
			List<UserVO> list = userService.getUsers(parent);
	        if(list==null || list.size()<=0 || list.get(0).getJfcenter()<new Double(jfzhuce)){
	        	renderJson(request, response, SysCode.PARAM_IS_ERROR, "积分不足");//用户名已注册
				return;
	        }
	        
			userVO.setPassword(MD5Util.MD5(password));
			userVO.setSafepwd(MD5Util.MD5(safepwd));
			Double zhucejf=new Double(jfzhuce);
			userVO.setJfzhuce(zhucejf);
			userVO.setJfold(jfzhuce);
			userVO.setJfDiya(new Double(jfzhuce)*num/100);
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
		
		String[] paramKey = {"userId","username","telephone","banknum","bankname"};
        Map<String, String> params = parseParams(request, "updateUser", paramKey);
        
        String userId = params.get("userId"); 
        String username = params.get("username"); 
        String telephone = params.get("telephone"); 
        String banknum = params.get("banknum"); 
        String bankname = params.get("bankname"); 
//        String password = params.get("password"); 
//        String safepwd = params.get("safepwd"); 
        if(StringUtils.isBlank(userId)){//shopId,userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        UserVO user =new UserVO();
        user.setId(Integer.parseInt(userId));
        List<UserVO> users = userService.getUsers(user);
        if(users == null || users.size()<=0){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, "没有该用户");
        	return;
        }
        user.setTelephone(telephone);
        user.setBankName(bankname);
        user.setBankNum(banknum);
        user.setUsername(username);
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
        if(!userid.equals(childlist.get(0).getPid())){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, "该账号非您的下级不能进行转赠！");
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
	
	//赠送秘钥
	@RequestMapping("/getGiveToken")
	public void getGiveToken(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userId","account"};
        Map<String, String> params = parseParams(request, "getGiveToken", paramKey);
        
        String userid = params.get("userId"); 
        String account = params.get("account"); //userId 不为空表示赠送秘钥，account 表示收入秘钥
        if(StringUtils.isBlank(userid) && StringUtils.isBlank(account)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        GiveTokenVO giveToken =new GiveTokenVO();
        giveToken.setPid(userid);
        giveToken.setAccount(account);
        try {
	        //更新
        	 List<GiveTokenVO> list = userService.getGiveToken(giveToken);
	    	if(list.size()>0){
	    		renderJson(request, response, SysCode.SUCCESS, list);
			}else{
				renderJson(request, response, SysCode.SUCCESS, list);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getGiveToken()`````"+e.getMessage());
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
	
	/**
	 * 上传文件
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping("/file/upload")
	public Map<String, Object> fileUpload( @RequestParam MultipartFile[] file) throws IOException{
		
		List<String> urls = new ArrayList<String>();
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			for(MultipartFile myfile : file){  
			        if(myfile.isEmpty()){  
			        	logger.warn("文件未上传");  
			        }else{  
			            logger.debug("文件长度: " + myfile.getSize());  
			            logger.debug("文件类型: " + myfile.getContentType());  
			            logger.debug("文件名称: " + myfile.getName());  
			            logger.debug("文件原名: " + myfile.getOriginalFilename());  
			            String ext =  FilenameUtils.getExtension(myfile.getOriginalFilename());
			            String reName = RandomStringUtils.randomAlphanumeric(32).toLowerCase() + "."+ ext;
			            String cdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
//			            /usr/local/nginx/html/pic
//			            String realPath = request.getSession().getServletContext().getRealPath("/upload")+ File.separator +cdate; 
			            String r="/usr/local/nginx/html/pic"+ File.separator +cdate;
			            String realPath = "D:\\projects\\bak\\springboot-adminlte-admin-master\\src\\main\\resources\\upload"+ File.separator +cdate; 
			            FileUtils.copyInputStreamToFile(myfile.getInputStream(), new File(r, reName)); 
			            urls.add("/pic/"+cdate+"/"+reName);
			        }  
			    }
			result.put("status", "success");
			result.put("urls",urls);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put("status", "error");
			return result;
		}  
	}

	
}
