package com.zcc.game.core;

import java.util.Collection;

/**
 * 
 * 版权：魔法衣橱 <br/>
 * 作者：dailing <br/>
 * 生成日期：2013-11-20 <br/>
 * 描述：通用工具类
 */
public class RoUtil {
	
	/**
	 * 
	 * 判断对象是否为空 <br/>
	 * 
	 * @param obj 目标对象 <br/>
	 * @return true 为空 false 不为空 <br/>
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj){
		if(obj==null){
			return true;
		}
		
		if(obj.toString().trim().isEmpty()){
			return true;
		}
		
		if(obj instanceof Collection){
			return ((Collection)obj).size()==0;
		}
		
		return false;
	}

}
