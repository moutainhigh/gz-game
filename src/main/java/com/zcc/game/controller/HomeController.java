package com.zcc.game.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zcc.game.common.OrderJob;
import com.zcc.game.common.QuartzJobUtils;
import com.zcc.game.common.SysCode;
import com.zcc.game.mq.RabbitMQSender;
import com.zcc.game.service.HomeService;
import com.zcc.game.service.UserService;
import com.zcc.game.utils.DateUtil;
import com.zcc.game.utils.MD5Util;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.DataVO;
import com.zcc.game.vo.MessageVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.PoolVO;
import com.zcc.game.vo.TaskVO;
import com.zcc.game.vo.TokenVO;
import com.zcc.game.vo.UserVO;

@Controller
@RequestMapping("/home")
public class HomeController extends BaseController{

	@Autowired
	private HomeService homeService;
	@Autowired
	private UserService userService;
	
	@Autowired
	private RabbitMQSender rabbitMQSender;
	
	//获取公告
	@RequestMapping("/getNotice")
	public void getNotice(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {};
        parseParams(request, "getNotice", paramKey);
        try {
	        //获取前五条公告数据
	    	List<NoticeVO> result = homeService.getNotices(new NoticeVO());
	    	if(result !=null && result.size()>0){
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
	//获取挂卖信息
	@RequestMapping("/getBusiness")
	public void getBusiness(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"status","userid"};
		Map<String, String> params = parseParams(request, "getBusiness", paramKey);
        String status = params.get("status"); //1待售，2交易中，3，完成，4，过期
        String userid = params.get("userid"); 
//        if(StringUtils.isBlank(status) ){//
//        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
//        	return;
//        }
        
        BusinessVO business = new BusinessVO();
        business.setStatus(status);
        business.setUserid(userid);
        try {
	        //获取挂卖信息
	    	List<BusinessVO> result = homeService.getBusiness(business);
	    	if(result !=null && result.size()>0){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getBusiness()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
    
	//增添挂卖信息
	@RequestMapping("/addBusiness")
	public void addBusiness(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"selljf","userid","safepwd"};
		Map<String, String> params = parseParams(request, "addBusiness", paramKey);
		String selljf = params.get("selljf"); 
		String userid = params.get("userid"); 
		String safepwd = params.get("safepwd"); 
		
		if(StringUtils.isBlank(selljf) || StringUtils.isBlank(userid) || StringUtils.isBlank(safepwd)){//userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        BusinessVO business = new BusinessVO();
        business.setUserid(userid);
        business.setSelljf(selljf);
        //验证用户信息
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userid));
        user.setStatus("0");
        List<UserVO> users = userService.getUsers(user);
        if(users ==null || users.size()<=0){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        int businessjf=users.get(0).getJfbusiness();
        int pretake=users.get(0).getPretake();//预扣减
    	String pwd=users.get(0).getSafepwd();
    	if(businessjf-pretake<Integer.parseInt(selljf) || !MD5Util.MD5(safepwd).equals(pwd)){
    		renderJson(request, response, SysCode.PARAM_IS_ERROR, "积分不足或密码错误");
        	return;
    	}
    	
        try {
	        //添加挂卖
	    	int result = homeService.addBusiness(business);
	    	if(result ==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addBusiness()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	//购买/确认收款/
	@RequestMapping("/updateBusiness")
	public void updateBusiness(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"id","status","userid","safepwd"};
		Map<String, String> params = parseParams(request, "addBusiness", paramKey);
		String status = params.get("status"); 
		String userid = params.get("userid"); 
		String safepwd = params.get("safepwd"); 
		String id = params.get("id"); 
		
		if(StringUtils.isBlank(status) || StringUtils.isBlank(id)){//userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
        BusinessVO business = new BusinessVO();
        business.setId(Integer.parseInt(id));
        business.setStatus(status);//1,售卖中，2交易中，3交易成功
        if("2".equals(status)){
        	business.setBuyerid(userid);
        	business.setConfigtime(DateUtil.addDay(new Date(),1));//24小时后
            business.setBuytime(new Date());
		}else if("3".equals(status)){//卖方确认收到款，积分转换，状态变更，
			//验证用户信息
	        UserVO user=new UserVO();
	        user.setId(Integer.parseInt(userid));
	        List<UserVO> users = userService.getUsers(user);
	        if(users ==null || users.size()<=0){
	        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
	        	return;
	        }
	    	String pwd=users.get(0).getSafepwd();
	    	if(!MD5Util.MD5(pwd).equals(safepwd)){
	    		renderJson(request, response, SysCode.PARAM_IS_ERROR, "密码错误");
	        	return;
	    	}
			business.setFinishtime(new Date());
		}else if("4".equals(status)){//过期未付款
			
		}
        
        try {
	        //添加挂卖
	    	int result = homeService.updateBusiness(business);
	    	if(result ==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addBusiness()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	//获取任务信息
	@RequestMapping("/getTask")
	public void getTask(HttpServletRequest request,HttpServletResponse response){
		String[] paramKey = {"id"};
		Map<String, String> params = parseParams(request, "getTask", paramKey);
        String id = params.get("id"); 
        if(StringUtils.isBlank(id) ){//
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        
        TaskVO task = new TaskVO();
        task.setId(Integer.parseInt(id));
        try {
	        //获取挂卖信息
	    	List<TaskVO> result = homeService.getTask(task);
	    	if(result !=null && result.size()>0){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getTask()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	//增添任务信息
	@RequestMapping("/addTask")
	public void addTask(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"title","status","userid"};
		Map<String, String> params = parseParams(request, "addTask", paramKey);
		String status = params.get("status"); 
		String userid = params.get("userid"); 
		String title = params.get("title"); 
		
		if(StringUtils.isBlank(title) || StringUtils.isBlank(userid) || StringUtils.isBlank(status)){//userID不能为空
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        TaskVO task = new TaskVO();
        task.setUserid(userid);
        task.setStatus(status);
        task.setTitle(title);
        task.setTaskjf("100");//后台设置
        //验证今日未赢过，
        try {
	        //添加任务
	    	int result = homeService.addTask(task);
	    	if(result ==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addTask()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}		
	//获取开奖
	@RequestMapping("/getData")
	public void getData(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"gmnum","id"};
		Map<String, String> params = parseParams(request, "addTask", paramKey);
		String id = params.get("id"); 
		String gmnum = params.get("gmnum"); 
		
		DataVO data=new DataVO();
		data.setId(Integer.parseInt(id));
		data.setGmnum(gmnum);
        try {
	        //获取开奖数据
	    	List<DataVO> result = homeService.getData(new DataVO());
	    	if(result !=null && result.size()>0){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getData()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	//增添开奖信息
	@RequestMapping("/addData")
	public void addData(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"gmnum","gm1","gm2","gm3","gm4","gm5"};
		Map<String, String> params = parseParams(request, "addData", paramKey);
		String gmnum = params.get("gmnum"); 
		String gm1 = params.get("gm1"); 
		String gm2 = params.get("gm2"); 
		String gm3 = params.get("gm3"); 
		String gm4 = params.get("gm4"); 
		String gm5 = params.get("gm5"); 
		
		if(StringUtils.isBlank(gmnum) || StringUtils.isBlank(gm1) || StringUtils.isBlank(gm2)
				|| StringUtils.isBlank(gm3) || StringUtils.isBlank(gm4)|| StringUtils.isBlank(gm4)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        DataVO data = new DataVO();
        data.setGmnum(gmnum);
        data.setGm1(gm1);
        data.setGm2(gm2);
        data.setGm3(gm3);
        data.setGm4(gm4);
        data.setGm5(gm5);
        getDataInfo(data);
        //验证今日未赢过，
        try {
	        //添加任务
	    	int result = homeService.addData(data);
	    	if(result ==1){
	    		rabbitMQSender.send("test-queue",data.getGmnum());
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addData()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}	
	private void getDataInfo(DataVO data){
		int gm1=Integer.parseInt(data.getGm1());
		int gm2=Integer.parseInt(data.getGm2());
		int gm3=Integer.parseInt(data.getGm3());
		int gm4=Integer.parseInt(data.getGm4());
		int gm5=Integer.parseInt(data.getGm5());
		int gmsum=gm1+gm2+gm3+gm4+gm5;
		data.setGmsum(gmsum+"");//总和
		data.setBgm1(isBig(gm1));
		data.setBgm2(isBig(gm2));
		data.setBgm3(isBig(gm3));
		data.setBgm4(isBig(gm4));
		data.setBgm5(isBig(gm5));
		data.setBgmsum(isBig(gmsum));
		data.setSgm1(isSign(gm1));
		data.setSgm2(isSign(gm2));
		data.setSgm3(isSign(gm3));
		data.setSgm4(isSign(gm4));
		data.setSgm5(isSign(gm5));
		data.setSgmsum(isSign(gmsum));
	}
	private String isBig(int num){
		if(num>4){
			return "大";
		}else{
			return "小";
		}
	}
	private String isSign(int num){
		if(num%2==0){
			return "双";
		}else{
			return "单";
		}
	}
	
	//增添下注信息
	@RequestMapping("/addPool")
	public void addPool(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid","jf","type","gmnum","count"};
		Map<String, String> params = parseParams(request, "addPool", paramKey);
		String userid = params.get("userid"); 
		String jf = params.get("jf"); 
		String type = params.get("type"); 
		String gmnum = params.get("gmnum"); 
		String count = params.get("count"); 
		
		if(StringUtils.isBlank(userid) || StringUtils.isBlank(jf) || StringUtils.isBlank(type)
				|| StringUtils.isBlank(gmnum) || StringUtils.isBlank(count)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
        PoolVO data = new PoolVO();
        data.setUserid(userid);
        data.setJf(jf);
        data.setBuyinfo(type);
        data.setGmnum(gmnum);
        int win=Integer.parseInt(jf);
        int w=Integer.parseInt(count)*win*198;
        int n=Integer.parseInt(count)*win*98;
        data.setWinjf(n+"");
        data.setGetjf(w+"");
        data.setCount(Integer.parseInt(count));
        data.setSumjf(Integer.parseInt(count)*win);//合计积分
        data.setBackjf("100");//返还一个积分
        //校验用户有效性和足够的积分。
        UserVO user=new UserVO();
        user.setId(Integer.parseInt(userid));
        List<UserVO> users = userService.getUsers(user);
        if(users==null || users.get(0).getJftask()<win){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, "积分不足");
        	return;
        }
        try {
	        //下注
	    	int result = homeService.addPool(data);
	    	if(result ==1){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addPool()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}	
	
	//查看投注记录
	@RequestMapping("/getPools")
	public void getPools(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid"};
		Map<String, String> params = parseParams(request, "getPools", paramKey);
		String userid = params.get("userid"); 
		
		if(StringUtils.isBlank(userid) ){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
		PoolVO pool=new PoolVO();
		pool.setUserid(userid);
        try {
	        //获取开奖数据
	    	List<PoolVO> result = homeService.getPools(pool);
	    	if(result !=null && result.size()>0){
	    		renderJson(request, response, SysCode.SUCCESS, result);
			}else{
				renderJson(request, response, SysCode.SUCCESS, result);
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getPools()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	
	//添加留言
	@RequestMapping("/addMessage")
	public void addMessage(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid","content"};
		Map<String, String> params = parseParams(request, "addMessage", paramKey);
		String userid = params.get("userid"); 
		String content = params.get("content"); 
		
		if(StringUtils.isBlank(userid) ||StringUtils.isBlank(content)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
		MessageVO message=new MessageVO();
		message.setUserid(userid);
		message.setContent(content);
        try {
	        //添加留言
	    	int result = homeService.addMessage(message);
	    	renderJson(request, response, SysCode.SUCCESS, result);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addMessage()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	//查看留言
	@RequestMapping("/getMessages")
	public void getMessages(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid"};
		Map<String, String> params = parseParams(request, "getMessages", paramKey);
		String userid = params.get("userid"); 
		
		if(StringUtils.isBlank(userid) ){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
		MessageVO message=new MessageVO();
		message.setUserid(userid);
        try {
	        //添加留言
	    	List<MessageVO> result = homeService.getMessages(message);
	    	renderJson(request, response, SysCode.SUCCESS, result);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getMessages()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	//添加赠送秘钥记录
	@RequestMapping("/addToken")
	public void addToken(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid","account","tokennum","safepwd"};
		Map<String, String> params = parseParams(request, "addToken", paramKey);
		String userid = params.get("userid"); 
		String account = params.get("account"); 
		String tokennum = params.get("tokennum"); 
		String safepwd = params.get("safepwd"); 
		
		if(StringUtils.isBlank(userid) ||StringUtils.isBlank(account)||
				StringUtils.isBlank(tokennum) ||StringUtils.isBlank(safepwd)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
		TokenVO token=new TokenVO();
		token.setUserid(userid);
		token.setAccount(account);
		token.setTokennum(tokennum);
		
		UserVO user=new UserVO();
		user.setId(Integer.parseInt(userid));
		List<UserVO> users = userService.getUsers(user);
		if(users.size()<=0 || !users.get(0).getSafepwd().equals(MD5Util.MD5(safepwd))){
			renderJson(request, response, SysCode.PARAM_IS_ERROR, "安全码有误");
        	return;
		}
		if(users.get(0).getTaskToken()<Integer.parseInt(tokennum)){
			renderJson(request, response, SysCode.PARAM_IS_ERROR, "秘钥不足");
        	return;
		}
		int num=0;
		List<UserVO> childUsers = userService.getChilds(user);
		for (int i = 0; i < childUsers.size(); i++) {
			String userAccount = childUsers.get(i).getAccount();
			if(userAccount.equals(account)){
				num=1;
				break;
			}
		}
		if(num==0){
			renderJson(request, response, SysCode.PARAM_IS_ERROR, "只能给下级赠送秘钥");
        	return;
		}
        try {
	        //赠送秘钥
	    	int result = homeService.addToken(token);
	    	renderJson(request, response, SysCode.SUCCESS, result);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````addToken()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
	//查看秘钥
	@RequestMapping("/getTokens")
	public void getTokens(HttpServletRequest request,HttpServletResponse response){
		
		String[] paramKey = {"userid","account"};
		Map<String, String> params = parseParams(request, "getTokens", paramKey);
		String userid = params.get("userid"); 
		String account = params.get("account"); 
		
		if(StringUtils.isBlank(userid) && StringUtils.isBlank(account)){
        	renderJson(request, response, SysCode.PARAM_IS_ERROR, null);
        	return;
        }
		
		TokenVO token=new TokenVO();
		token.setUserid(userid);
		token.setAccount(account);
        try {
	        //获取赠送秘钥列表
	    	List<TokenVO> result = homeService.getTokens(token);
	    	renderJson(request, response, SysCode.SUCCESS, result);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.info("`````method``````getTokens()`````"+e.getMessage());
			renderJson(request, response, SysCode.SYS_ERR, e.getMessage());
		}
	}
}
