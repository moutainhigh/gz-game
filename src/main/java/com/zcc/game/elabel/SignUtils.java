package com.walmart.mobile.checkout.utils;

import org.apache.commons.codec.digest.DigestUtils;

import com.walmart.mobile.checkout.exception.ApplicationException;

public class SignUtils {

	public static String getSignBySHA512(String param, String token) throws Exception {
		StringBuilder resultStr = new StringBuilder("");
		resultStr.append(token).append(param).append(token);
		return DigestUtils.sha512Hex(resultStr.toString()).toUpperCase();
	}
	
	/**
	 * 计算签名
	 * 
	 */
	public static String getSign(String requestJson, String token) throws ApplicationException {
		String sign = null;
		try {
			sign = SignUtils.getSignBySHA512(requestJson, token);
		} catch (Exception e) {
			throw new ApplicationException("--- getSignBySHA512 occur exp.", e);
		}

		return sign;
	}
}
