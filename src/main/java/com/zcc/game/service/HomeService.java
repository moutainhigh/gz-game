package com.zcc.game.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zcc.game.common.BusinessType;
import com.zcc.game.common.HttpRequest;
import com.zcc.game.common.OpenDataJob;
import com.zcc.game.common.OrderJob;
import com.zcc.game.common.QuartzJobUtils;
import com.zcc.game.mapper.HomeMapper;
import com.zcc.game.mapper.UserMapper;
import com.zcc.game.utils.DateUtil;
import com.zcc.game.vo.BusinessVO;
import com.zcc.game.vo.ChangeCenterVO;
import com.zcc.game.vo.DataVO;
import com.zcc.game.vo.GiveTokenVO;
import com.zcc.game.vo.MessageVO;
import com.zcc.game.vo.NoticeVO;
import com.zcc.game.vo.PailongVO;
import com.zcc.game.vo.PoolVO;
import com.zcc.game.vo.TaskVO;
import com.zcc.game.vo.TokenVO;
import com.zcc.game.vo.UserVO;
import com.zcc.game.vo.base.OpenData;

@Service
public class HomeService {

	@Autowired
	private HomeMapper homeMapper;
	@Autowired
	private UserMapper userMapper;
	
	//获取今天赢的记录数
	public List<PoolVO> getWinData(PoolVO notice){
		return homeMapper.getWinData(notice);
	}
	/**
     * 获取定时任务执行时间
     * @param fixTime
     * @return
     */
    private String getCronExpressionByFixTime(int num) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.SECOND, num);
    	Date date = calendar.getTime();
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
    	
        return sdf.format(date);
    }
    
	public List<NoticeVO> getNotices(NoticeVO notice){
		return homeMapper.getNotices(notice);
	}
	public List<PailongVO> getPailong(PailongVO pailong){
		return homeMapper.getPailong(pailong);
	}
	public List<BusinessVO> getBusiness(BusinessVO business){
		return homeMapper.getBusiness(business);
	}
	public List<BusinessVO> getBusinessBySell(BusinessVO business){
		return homeMapper.getBusinessBySell(business);
	}
	
	public List<BusinessVO> getBuyJf(BusinessVO business){
		return homeMapper.getBuyJf(business);
	}
	public int addBusiness(BusinessVO business) throws Exception{
		int num = homeMapper.addBusiness(business);
		return num;
	}
	
	public int addBusinessLog(BusinessVO business) throws Exception{
		int num = homeMapper.addBusinessLog(business);
		return num;
	}
	public int addMessage(MessageVO message){
		return homeMapper.addMessage(message);
	}
	public List<MessageVO> getMessages(MessageVO message){
		return homeMapper.getMessages(message);
	}
	public int addToken(TokenVO token){
		return homeMapper.addToken(token);
	}
	public List<GiveTokenVO> getTokens(GiveTokenVO token){
		return homeMapper.getTokens(token);
	}
	//回调，检查交易数据,未打款，封号，1，待售，2，交易中，3，已完成。4，购买中
	@Transactional
	public void checkBusiness(String id) throws Exception{
		BusinessVO business=new BusinessVO();
		business.setId(Integer.parseInt(id));
		List<BusinessVO> bs=homeMapper.getBusiness(business);
		String status = bs.get(0).getStatus();
		String userid = bs.get(0).getUserid();
		if(BusinessType.交易中.getValue().equals(status)){//待交易，封号，记录交易日志。
			business.setStatus(BusinessType.待购买.getValue());//返回待交易状态
			homeMapper.updateBusiness(business);
			UserVO user=new UserVO();
			user.setId(Integer.parseInt(userid));
			user.setStatus("1");//无效账号
			userMapper.updateUser(user);
			//记录购买日志
			BusinessVO newBs=new BusinessVO();
			newBs.setUserid(bs.get(0).getUserid());
			newBs.setSelljf(bs.get(0).getSelljf());
			newBs.setBuyerid(bs.get(0).getBuyerid());
			homeMapper.addBusinessLog(newBs);
		}else if(BusinessType.购买中.getValue().equals(status)){//检查凭证是否上传成功
			String voucher = bs.get(0).getVoucher();//上传凭证
			if(voucher==null || "".equals(voucher)){//未上传成功，变为待购买
				business.setStatus(BusinessType.待购买.getValue());//返回待交易状态
				business.setBuyerid(null);
			}else{//上传成功，变为交易中
				business.setStatus(BusinessType.交易中.getValue());//返回待交易状态
				business.setConfigtime(DateUtil.addDay(new Date(),1));//24小时后
				business.setBuytime(new Date());
				//添加定时任务，24小时之内付款，否则封号，解压状态继续售卖。
				// 添加定时任务
		     	JobDataMap jobDataMap = new JobDataMap();
		        jobDataMap.put("businessid", business.getId()+"");
		        jobDataMap.put("HomeService", this);
		        
		        //获取后台参数
		        QuartzJobUtils.addJob("CANCEL_ORDER_"+business.getId(), OrderJob.class, getCronExpressionByFixTime(60*60*24), jobDataMap);
			}
			homeMapper.updateBusiness(business);
		}
	}
	
	@Transactional
	public int updateBusiness(BusinessVO business) throws Exception{
		if(BusinessType.购买中.getValue().equals(business.getStatus())){//购买中，5分钟内上传凭证。
//			BusinessVO addbusiness = new BusinessVO();
//			addbusiness.setUserid(business.getUserid());
//			addbusiness.setSelljf(business.getSelljf());
//	        homeMapper.addBusiness(addbusiness);
//	        UserVO user=new UserVO();
//	        user.setId(Integer.parseInt(business.getBuyerid()));
//	        user.setStatus("1");//封号
//	        userMapper.updateUser(user);
			// 添加定时任务
	     	JobDataMap jobDataMap = new JobDataMap();
	        jobDataMap.put("businessid", business.getId()+"");
	        jobDataMap.put("HomeService", this);
	        
	        //获取后台参数
	        QuartzJobUtils.addJob("CANCEL_ORDER_"+business.getId(), OrderJob.class, getCronExpressionByFixTime(60*5), jobDataMap);
		}else if(BusinessType.已完成.getValue().equals(business.getStatus())){//卖家确认收款，积分转换，交易完成
			BusinessVO tempVO=new BusinessVO();
			tempVO.setId(business.getId());
			tempVO.setStatus("2");
			List<BusinessVO> list = homeMapper.getBusiness(tempVO);
			String sellJf=list.get(0).getSelljf();
			Double jf=new Double(sellJf);
			String buyerId=list.get(0).getBuyerid();
			String sellerId=list.get(0).getUserid();
			UserVO user=new UserVO();
			user.setId(Integer.parseInt(buyerId));
			user.setJfcenter(jf);
			userMapper.updateUser(user);//购买人增加积分
//			UserVO user2=new UserVO();
//			user2.setId(Integer.parseInt(sellerId));
//			user2.setJfbusiness(-jf);
//			user2.setPretake(-jf);
//			userMapper.updateUser(user2);//卖出人减去积分
		}else if(BusinessType.交易中.getValue().equals(business.getStatus())){//买家购买，状态变为交易中
			//添加定时任务，24小时之内付款，否则封号，解压状态继续售卖。
			// 添加定时任务
	     	JobDataMap jobDataMap = new JobDataMap();
	        jobDataMap.put("businessid", business.getId()+"");
	        jobDataMap.put("HomeService", this);
	        
	        //获取后台参数
	        QuartzJobUtils.addJob("CANCEL_ORDER_"+business.getId(), OrderJob.class, getCronExpressionByFixTime(60*60*24), jobDataMap);
		}
		return homeMapper.updateBusiness(business);
	}
	
	//任务信息
	@Transactional
	public int addTask(TaskVO task){
		if(task.getStatus().equals("2")){//审批通过
			UserVO user=new UserVO();
			user.setId(Integer.parseInt(task.getUserid()));
			user.setPrejftask(new Double(task.getTaskjf()));
			userMapper.updateUser(user);
		}
		return homeMapper.addTask(task);
	}
	public int updateTask(TaskVO task){
		return homeMapper.updateTask(task);
	}
	public List<TaskVO> getTask(TaskVO task){
		return homeMapper.getTask(task);
	}
	public List<TaskVO> getTaskByStatus(TaskVO task){
		return homeMapper.getTaskByStatus(task);
	}
	//开奖信息
	public int addData(DataVO task) throws Exception{
		int num = homeMapper.addData(task);
		if(num>0){
			JobDataMap jobDataMap = new JobDataMap();
	        jobDataMap.put("gmnum", task.getGmnum());
	        jobDataMap.put("HomeService", this);
	        
	        //获取后台参数
	        QuartzJobUtils.addJob("CANCEL_DATA_"+task.getGmnum(), OpenDataJob.class, getCronExpressionByFixTime(60), jobDataMap);
		}
		return num;
	}
	public List<DataVO> getData(DataVO data){
		taskData();
		return homeMapper.getData(data);
	}
	
	public void taskData(){
		String resul=HttpRequest.sendGet("http://ho.apiplus.net/newly.do?token=tf5d11ebbf5a9a989k&code=cqssc&rows=1&format=json");
		JSONObject obj=JSONObject.parseObject(resul);
		List<OpenData> list = JSONObject.parseArray(obj.get("data").toString(),OpenData.class);
		if(list.size()<=0){
			return ;
		}
		OpenData openData=list.get(0);//最新一期开奖
		String num=openData.getExpect();//期号
		String code=openData.getOpencode();//开奖结果
//		String time=openData.getOpentime();
		DataVO vo=new DataVO();
		String strData []=code.split(",");
		vo.setGmnum(num);
		vo.setGm1(strData[0]);
		vo.setGm2(strData[1]);
		vo.setGm3(strData[2]);
		vo.setGm4(strData[3]);
		vo.setGm5(strData[4]);
		getDataInfo(vo);
		List<DataVO> dataList = homeMapper.getData(vo);
		if(dataList==null || dataList.size()<=0){
			homeMapper.addData(vo);
			//获取到数据，开奖,结算。
			updatePoolJF(vo.getGmnum());//开奖期号
			//获取排龙信息
			getPaiLongData();
		}
	}
	
	//获取两面排龙
	public void getPaiLongData(){
		List<DataVO> list = homeMapper.getDataByPaiLong(null);
		List<PailongVO> paiLongList=new ArrayList<PailongVO>();
		String bgm1="";
		String bgm2="";
		String bgm3="";
		String bgm4="";
		String bgm5="";
		String sgm1="";
		String sgm2="";
		String sgm3="";
		String sgm4="";
		String sgm5="";
		String sumbs="";
		String sumsd="";
		int bg1=1;
		int bg2=1;
		int bg3=1;
		int bg4=1;
		int bg5=1;
		int sg1=1;
		int sg2=1;
		int sg3=1;
		int sg4=1;
		int sg5=1;
		int sumb=1;
		int sums=1;
		//1号球-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				bgm1=dataVO.getBgm1();
			}else{
				if(bgm1.equals(dataVO.getBgm1())){//相同
					bg1++;
				}else{
					break;
				}
			}
		}
		if(bg1>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(bg1+"期");
			if(bgm1.equals("大")){
				pailong.setNumber("big1");
				pailong.setName("1号球-大");
			}else{
				pailong.setNumber("small1");
				pailong.setName("1号球-小");
			}
			paiLongList.add(pailong);
		}
		//2号球-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				bgm2=dataVO.getBgm2();
			}else{
				if(bgm2.equals(dataVO.getBgm2())){//相同
					bg2++;
				}else{
					break;
				}
			}
		}
		if(bg2>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(bg2+"期");
			if(bgm2.equals("大")){
				pailong.setNumber("big2");
				pailong.setName("2号球-大");
			}else{
				pailong.setNumber("small2");
				pailong.setName("2号球-小");
			}
			paiLongList.add(pailong);
		}
		//3号球-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				bgm3=dataVO.getBgm3();
			}else{
				if(bgm3.equals(dataVO.getBgm3())){//相同
					bg3++;
				}else{
					break;
				}
			}
		}
		if(bg3>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(bg3+"期");
			if(bgm3.equals("大")){
				pailong.setNumber("big3");
				pailong.setName("3号球-大");
			}else{
				pailong.setNumber("small3");
				pailong.setName("3号球-小");
			}
			paiLongList.add(pailong);
		}
		//4号球-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				bgm4=dataVO.getBgm4();
			}else{
				if(bgm4.equals(dataVO.getBgm4())){//相同
					bg4++;
				}else{
					break;
				}
			}
		}
		if(bg4>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(bg4+"期");
			if(bgm4.equals("大")){
				pailong.setNumber("big4");
				pailong.setName("4号球-大");
			}else{
				pailong.setNumber("small4");
				pailong.setName("4号球-小");
			}
			paiLongList.add(pailong);
		}
		//5号球-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				bgm5=dataVO.getBgm5();
			}else{
				if(bgm5.equals(dataVO.getBgm5())){//相同
					bg5++;
				}else{
					break;
				}
			}
		}
		if(bg5>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(bg5+"期");
			if(bgm5.equals("大")){
				pailong.setNumber("big5");
				pailong.setName("5号球-大");
			}else{
				pailong.setNumber("small5");
				pailong.setName("5号球-小");
			}
			paiLongList.add(pailong);
		}
		//1号球-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sgm1=dataVO.getSgm1();
			}else{
				if(sgm1.equals(dataVO.getSgm1())){//相同
					sg1++;
				}else{
					break;
				}
			}
		}
		if(sg1>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sg1+"期");
			if(sgm1.equals("单")){
				pailong.setNumber("single1");
				pailong.setName("1号球-单");
			}else{
				pailong.setNumber("doub1");
				pailong.setName("1号球-双");
			}
			paiLongList.add(pailong);
		}
		//2号球-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sgm2=dataVO.getSgm2();
			}else{
				if(sgm2.equals(dataVO.getSgm2())){//相同
					sg2++;
				}else{
					break;
				}
			}
		}
		if(sg2>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sg2+"期");
			if(sgm2.equals("单")){
				pailong.setNumber("single2");
				pailong.setName("2号球-单");
			}else{
				pailong.setNumber("doub2");
				pailong.setName("2号球-双");
			}
			paiLongList.add(pailong);
		}
		//3号球-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sgm3=dataVO.getSgm3();
			}else{
				if(sgm3.equals(dataVO.getSgm3())){//相同
					sg3++;
				}else{
					break;
				}
			}
		}
		if(sg3>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sg3+"期");
			if(sgm3.equals("单")){
				pailong.setNumber("single3");
				pailong.setName("3号球-单");
			}else{
				pailong.setNumber("doub3");
				pailong.setName("3号球-双");
			}
			paiLongList.add(pailong);
		}
		//4号球-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sgm4=dataVO.getSgm4();
			}else{
				if(sgm4.equals(dataVO.getSgm4())){//相同
					sg4++;
				}else{
					break;
				}
			}
		}
		if(sg4>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sg4+"期");
			if(sgm4.equals("单")){
				pailong.setNumber("single4");
				pailong.setName("4号球-单");
			}else{
				pailong.setNumber("doub4");
				pailong.setName("4号球-双");
			}
			paiLongList.add(pailong);
		}
		//5号球-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sgm5=dataVO.getSgm5();
			}else{
				if(sgm5.equals(dataVO.getSgm5())){//相同
					sg5++;
				}else{
					break;
				}
			}
		}
		if(sg5>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sg5+"期");
			if(sgm5.equals("单")){
				pailong.setNumber("single5");
				pailong.setName("5号球-单");
			}else{
				pailong.setNumber("doub5");
				pailong.setName("5号球-双");
			}
			paiLongList.add(pailong);
		}
		//总和-大小
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sumbs=dataVO.getBgmsum();
			}else{
				if(sumbs.equals(dataVO.getBgmsum())){//相同
					sumb++;
				}else{
					break;
				}
			}
		}
		if(sumb>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sumb+"期");
			if(sumbs.equals("大")){
				pailong.setNumber("sumbig");
				pailong.setName("总和-大");
			}else{
				pailong.setNumber("sumsmall");
				pailong.setName("总和-小");
			}
			paiLongList.add(pailong);
		}
		//总和-单双
		for (int i = 0; i < list.size(); i++) {
			DataVO dataVO=list.get(i);
			if(i==0){
				sumsd=dataVO.getSgmsum();
			}else{
				if(sumsd.equals(dataVO.getSgmsum())){//相同
					sums++;
				}else{
					break;
				}
			}
		}
		if(sums>1){
			PailongVO pailong=new PailongVO();
			pailong.setContent(sums+"期");
			if(sumsd.equals("单")){
				pailong.setNumber("sumsingle");
				pailong.setName("总和-单");
			}else{
				pailong.setNumber("sumdoub");
				pailong.setName("总和-双");
			}
			paiLongList.add(pailong);
		}				
		homeMapper.addPaiLong(paiLongList);
	}
	@Transactional
	public int addPool(List<PoolVO> pools){
		for (int i = 0; i < pools.size(); i++) {
			homeMapper.addPool(pools.get(i));
		}
		return 1;
	}
	public List<PoolVO> getPools(PoolVO pool){
		return homeMapper.getPools(pool);
	}
	
	//更新中奖用户-更新用户中奖状态，更新中奖用户积分
	public void updatePoolJF(String gmnum){
		PoolVO pool=new PoolVO();
		pool.setGmnum(gmnum);//开奖期号，如：20180818001
		DataVO data=new DataVO();
		data.setGmnum(gmnum);
		List<DataVO> datas=homeMapper.getData(data);
		data=datas.get(0);
		List<PoolVO> pools = homeMapper.getPools(pool);
		if(pools.size()>0){
			for (PoolVO p:pools) {
				int num=isWinPrize(p.getBuyinfo(), data);
				String back="";
				if(num>0){
					p.setStatus("1");//中奖
					double d=new Double(p.getGetjf())+new Double(p.getBackjf());
					back=d+"";
				}else{
					p.setStatus("2");//未中奖
					p.setWinjf("0");
					p.setGetjf("0");
					double d=new Double(p.getBackjf());
					back=d+"";
				}
				p.setLastup_date(new Date());
				addChnageCenter(back,"转入","投注转回",p.getUserid());
			}
			homeMapper.updatePools(pools);
		}
	}
	
	public void addChnageCenter(String num,String type,String status,String userId){
		ChangeCenterVO ch=new ChangeCenterVO();
		ch.setNum(num);
		ch.setStatus(status);
		ch.setType(type);
		ch.setUserid(userId);
		userMapper.addChangeCenter(ch);
	}
	
	public int isWinPrize(String buyInfo,DataVO data){
		int result=0;
		if("big1".equals(buyInfo) && "大".equals(data.getBgm1())){
			result=1;
		}else if("small1".equals(buyInfo) && "小".equals(data.getBgm1())){
			result=1;
		}else if("single1".equals(buyInfo) && "单".equals(data.getSgm1())){
			result=1;
		}else if("doub1".equals(buyInfo) && "双".equals(data.getSgm1())){
			result=1;
		}else if("big2".equals(buyInfo) && "大".equals(data.getBgm2())){
			result=1;
		}else if("small2".equals(buyInfo) && "小".equals(data.getBgm2())){
			result=1;
		}else if("single2".equals(buyInfo) && "单".equals(data.getSgm2())){
			result=1;
		}else if("doub2".equals(buyInfo) && "双".equals(data.getSgm2())){
			result=1;
		}else if("big3".equals(buyInfo) && "大".equals(data.getBgm3())){
			result=1;
		}else if("small3".equals(buyInfo) && "小".equals(data.getBgm3())){
			result=1;
		}else if("single3".equals(buyInfo) && "单".equals(data.getSgm3())){
			result=1;
		}else if("doub3".equals(buyInfo) && "双".equals(data.getSgm3())){
			result=1;
		}else if("big4".equals(buyInfo) && "大".equals(data.getBgm4())){
			result=1;
		}else if("small4".equals(buyInfo) && "小".equals(data.getBgm4())){
			result=1;
		}else if("single4".equals(buyInfo) && "单".equals(data.getSgm4())){
			result=1;
		}else if("doub4".equals(buyInfo) && "双".equals(data.getSgm4())){
			result=1;
		}else if("big5".equals(buyInfo) && "大".equals(data.getBgm5())){
			result=1;
		}else if("small5".equals(buyInfo) && "小".equals(data.getBgm5())){
			result=1;
		}else if("single5".equals(buyInfo) && "单".equals(data.getSgm5())){
			result=1;
		}else if("doub5".equals(buyInfo) && "双".equals(data.getSgm5())){
			result=1;
		}else if("sumbig".equals(buyInfo) && "大".equals(data.getBgmsum())){
			result=1;
		}else if("sumsmall".equals(buyInfo) && "小".equals(data.getBgmsum())){
			result=1;
		}else if("sumsingle".equals(buyInfo) && "单".equals(data.getSgmsum())){
			result=1;
		}else if("sumdoub".equals(buyInfo) && "双".equals(data.getSgmsum())){
			result=1;
		}else if("long".equals(buyInfo) && Integer.parseInt(data.getGm1())>Integer.parseInt(data.getGm5())){
			result=1;
		}else if("hu".equals(buyInfo) && Integer.parseInt(data.getGm1())<=Integer.parseInt(data.getGm5())){
			result=1;
		}
		return result;
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
		data.setBgmsum(isSumBig(gmsum));
		data.setSgm1(isSign(gm1));
		data.setSgm2(isSign(gm2));
		data.setSgm3(isSign(gm3));
		data.setSgm4(isSign(gm4));
		data.setSgm5(isSign(gm5));
		data.setSgmsum(isSign(gmsum));
	}
	private String isSumBig(int num){
		if(num>22){
			return "大";
		}else{
			return "小";
		}
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
}
