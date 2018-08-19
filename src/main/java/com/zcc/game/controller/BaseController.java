package com.zcc.game.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
//import com.group.core.commons.Body;
//import com.group.core.commons.Head;
//import com.group.core.commons.ParamInfo;
//import com.group.core.commons.RequestJSON;
//import com.group.core.commons.ResponseJSON;
//import com.group.core.commons.SysCode;
//import com.group.utils.ResponseUtils;
//import com.group.utils.RoUtil;
//import com.group.utils.fastjsonUtils.JsonUtils;
import com.zcc.game.common.SysCode;
import com.zcc.game.core.Body;
import com.zcc.game.core.Head;
import com.zcc.game.core.ParamInfo;
import com.zcc.game.core.RequestJSON;
import com.zcc.game.core.ResponseJSON;
import com.zcc.game.core.ResponseUtils;
import com.zcc.game.core.RoUtil;
import com.zcc.game.core.fastjson.JsonUtils;

public class BaseController {

    protected static Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     * 准备参数
     * @param request
     * @param method
     * @param paramKey
     * @return
     */
    protected Map<String, String> parseParams(HttpServletRequest request, String method, String[] paramKey) {
        logger.info("请求接口：" + method );
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < paramKey.length; i++) {
            String param = request.getParameter(paramKey[i]);
            if (param != null) {
            	params.put(paramKey[i], param);
                logger.info("【text-- the key:" + paramKey[i] + " ,value :" + param + "】");
            } else {
                logger.info("【warn-- the key:" + paramKey[i] + " is empty or is null!】");
            }
        }
        return params;
    }
    
    /**
     * 准备ParamInfo对象
     * @param request
     * @param method
     * @param paramKey
     * @return
     */
    protected ParamInfo parseRequest(HttpServletRequest request, String method, String[] paramKey) {
    	//获取dataJson参数
        String jsonParam = request.getParameter("dataJson");
        logger.info("请求接口：" + method + "; 请求参数：" + jsonParam);
        
        Object jsonStr = "";
        String mac = UUID.randomUUID().toString();
        String sNumber = UUID.randomUUID().toString();
        String version = "V1.0";

        ParamInfo info = new ParamInfo();

        // 如果传入字符串为空,则提示参数不合法
        if (RoUtil.isEmpty(jsonParam)) {
            info.setState(SysCode.PARAM_IS_ERROR);
            info.setResponse(getResponseJSON(mac, method, sNumber, version, SysCode.PARAM_IS_ERROR, jsonStr));
            return info;
        }

        //将传入的参数转换成
        RequestJSON requestJSON = JSON.parseObject(jsonParam, RequestJSON.class);
        info.setRequestJSON(requestJSON);

        if (paramKey != null && paramKey.length > 0) {
            for (int i = 0; i < paramKey.length; i++) {
                String jp = requestJSON.getBody().getString(paramKey[i]);
                if (!RoUtil.isEmpty(jp)) {
                    info.getParams().put(paramKey[i], jp);
                    logger.info("【text-- the key:" + paramKey[i] + " ,value :" + jp + "】");
                } else {
                    logger.info("【warn-- the key:" + paramKey[i] + " is empty or is null!】");
                }
            }
        }

        info.setResponse(getResponseJSON(mac, method, sNumber, version, SysCode.SUCCESS, jsonStr));
        info.setState(SysCode.SUCCESS);
        return info;
    }
    /**
     * 构建返回给前端的JSON对象 <br/>
     * 构建返回给前端的JSON对象 <br/>
     * 
     * @param [mac]-[加密信息] <br/>
     * @param [method]-[后台调用方法] <br/>
     * @param [sNumber]-[序列号] <br/>
     * @param [version]-[版本号] <br/>
     * @param [code]-[返回消息消息码和消息] <br/>
     * @param [result]-[返回结果] <br/>
     * @return [ResponseJSON] 返回给前段JSON对象<br/>
     */
    protected ResponseJSON getResponseJSON(String mac, String method, String sNumber, String version, SysCode code,
            Object result) {
        ResponseJSON responseJSON = new ResponseJSON();
        responseJSON.setMac(mac);
        responseJSON.setHead(getResponseHead(method, sNumber, version));
        responseJSON.setBody(getResponseBody(code, result));
        return responseJSON;
    }
    /**
     * 构建输出到终端JSON 串HEAD <br/>
     * 构建输出到终端JSON 串HEAD <br/>
     * 
     * @param [method]-[后台调用方法名] <br/>
     * @param [sNumber]-[序列号] <br/>
     * @param [version]-[版本号] <br/>
     * @return [Head] 输出到终端JSON 串 HEAD<br/>
     */
    protected Head getResponseHead(String method, String sNumber, String version) {
        return new Head(method, sNumber, version);
    }
    /**
     * 构建输出到终端JSON 串BODY <br/>
     * 构建输出到终端JSON 串BODY <br/>
     * 
     * @param [SysCode]-[返回消息消息码 和消息] <br/>
     * @param [result]-[返回JSON 结果] <br/>
     * @return [Body] 输出到终端JSON串 BODY<br/>
     */
    protected Body getResponseBody(SysCode code, Object result) {
        Body body = new Body();
        body.setMessage(code.getMessage());
        body.setCode(code.getCode());
        body.setResult(result);
        return body;
    }
    /**
     * 描述：〈响应输出内容，包含跨域处理〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-03-13 <br/>
     * 
     * @param request 请求
     * @param response 响应
     * @param sysCode 系统代码
     * @param content JSON对象
     */
    protected void renderJson(HttpServletRequest request, HttpServletResponse response, SysCode sysCode,
            Object content) {
        renderJson(request, response, sysCode, content, null);
    }
    
    protected void renderJson(HttpServletRequest request, HttpServletResponse response, SysCode sysCode,
            Object content, Map<Class<?>, String[]> includes) {
        renderJson(request, response, sysCode, content, includes, null);
    }
    
    protected void renderJson(HttpServletRequest request, HttpServletResponse response, SysCode sysCode,
            Object content, Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", sysCode.getCode());
        result.put("message", sysCode.getMessage());
        result.put("result", content);
        
        String callback = request.getParameter("callback");
        if (!StringUtils.isEmpty(callback)) {
            ResponseUtils.renderText(response, callback + "(" + JSON.toJSON(result) + ");");
        } else {
            ResponseUtils.renderJson(response, JsonUtils.toJsonString(result, includes, excludes, false));
        }
    }

    /**
     * 描述：〈响应输出内容，包含跨域处理〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-03-13 <br/>
     * 
     * @param request 请求
     * @param response 响应
     * @param content JSON对象
     */
    protected void renderJson(HttpServletRequest request, HttpServletResponse response, Object content) {
        renderJson(request, response, content, null, null);
    }
    
    protected void renderJson(HttpServletRequest request, HttpServletResponse response, Object content,
            Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes) {
        String callback = request.getParameter("callback");
        if (StringUtils.isNotEmpty(callback)) {
            ResponseUtils.renderText(response, callback + "(" + JsonUtils.toJsonString(content, includes, excludes, true) + ");");
        } else {
            ResponseUtils.renderJson(response, JsonUtils.toJsonString(content, includes, excludes, false));
        }
    }
    
    /**
     * 描述：〈生成响应的Json内容〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-6-6 <br/>
     * 
     * @param response 请求
     * @param responseJson ResponseJSON对象
     */
    protected void renderResponseJson(HttpServletResponse response, ResponseJSON responseJson) {
        ResponseUtils.renderJson(response, JsonUtils.toJsonString(responseJson, false));
    }

    /**
     * 描述：〈生成响应的Json内容〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-6-6 <br/>
     * 
     * @param response 请求
     * @param responseJson 响应JSON对象
     * @param includes 包含属性
     */
    protected void renderResponseJson(HttpServletResponse response, ResponseJSON responseJson,
            Map<Class<?>, String[]> includes) {
        renderResponseJson(response, responseJson, includes, null);
    }
    
    /**
     * 描述：〈生成响应的Json内容〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-6-6 <br/>
     * 
     * @param response 请求
     * @param responseJson 响应JSON对象
     * @param includes 包含属性
     * @param excludes 排除属性
     */
    protected void renderResponseJson(HttpServletResponse response, ResponseJSON responseJson,
            Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes) {
        ResponseUtils.renderJson(response, JsonUtils.toJsonString(responseJson, includes, excludes, false));
    }

    protected String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
}
