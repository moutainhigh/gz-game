package com.zcc.game.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtil {
	
	//订单状态
	public static final String WAINTPAY="1";
	public static final String SENDING="2";
	public static final String WAINTPJ="3";
	public static final String HASFINISH="4";
	//数据处理失败
	public static final String SQLERROR="数据处理失败";
	public static final String USERERROR="用户验证失败";
	//支付类型
	public static final String PAYTYPE_XF="1";//消费
	public static final String PAYTYPE_CZ="0";//充值
	//系统参数编码
	public static final String APIKEY="D24rGg17ukj8h4G787nsIUw2bb2Skyhm";
	public static final String APPID="wx6751d7e1b89aee61";
	public static final String PARTNER="1407028102";
	public static final String SERVICEIP="112.74.20.36";
	
	
	public static final String APIKEY2="SboRT3SkdZPJsMTZCe13qzCe2h0LWPTE";
	public static final String APPID2="wxfbbcb501e55af077";
	public static final String PARTNER2="1489181562";
	public static final String appscrect="4ab277780d652c5c9fbb670ab493f9ef";
	
		
	public static List<String> getList(String param){
		List<String> statusList=new ArrayList<String>();
		String [] str=param.split(",");
		for (int i = 0; i < str.length; i++) {
			statusList.add(str[i]);
		}
		return statusList;
	}	
	
	//返回列对象
	public static Map<Class<?>, String[]> getObjectIncludes(final Class<?> c ,final String[] a){
		Map<Class<?>, String[]> includes = new HashMap<Class<?>, String[]>() {
            private static final long serialVersionUID = -5349178483472578926L;
            {
                put(c, a);
            }
        };
        return includes;
	}
	
	
	
}
