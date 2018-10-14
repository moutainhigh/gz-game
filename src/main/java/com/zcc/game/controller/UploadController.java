package com.zcc.game.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class UploadController {

	public static final Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	@RequestMapping("/upload")
	@ResponseBody
	public void testUploadFile(HttpServletRequest req,MultipartHttpServletRequest multiReq) throws IOException{
//		List<String> urls = new ArrayList<String>();
//		Map<String, Object> result = new HashMap<String, Object>();
//		
//		try {
//			for(MultipartFile myfile : file){  
//			        if(myfile.isEmpty()){  
//			        	logger.warn("文件未上传");  
//			        }else{  
//			            logger.debug("文件长度: " + myfile.getSize());  
//			            logger.debug("文件类型: " + myfile.getContentType());  
//			            logger.debug("文件名称: " + myfile.getName());  
//			            logger.debug("文件原名: " + myfile.getOriginalFilename());  
//			            String ext =  FilenameUtils.getExtension(myfile.getOriginalFilename());
//			            String reName = RandomStringUtils.randomAlphanumeric(32).toLowerCase() + "."+ ext;
//			            String cdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
////			            String realPath = request.getSession().getServletContext().getRealPath("/upload")+ File.separator +cdate; 
//			            String realPath = "D:\\projects\\bak\\springboot-adminlte-admin-master\\src\\main\\resources\\upload"+ File.separator +cdate; 
//			            FileUtils.copyInputStreamToFile(myfile.getInputStream(), new File(realPath, reName)); 
//			            urls.add("/upload/"+cdate+"/"+reName);
//			        }  
//			    }
//			result.put("status", "success");
//			result.put("urls",urls);
//			return result;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			result.put("status", "error");
//			return result;
//		}  
//		String path="/home/upload/1.jpg";
		String ext =  FilenameUtils.getExtension(multiReq.getFile("file").getOriginalFilename());
        String reName = RandomStringUtils.randomAlphanumeric(32).toLowerCase() + "."+ ext;
        String cdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		String path="/usr/local/nginx/html/pic/"+cdate;
//		String path="F:\\upload\\"+cdate;
		if(!new File(path).exists()){
			new File(path).mkdir();
		}
//		"F:\\upload\\1.jpg"
		FileOutputStream fos=new FileOutputStream(new File(path+File.separator+reName));
		FileInputStream in = (FileInputStream) multiReq.getFile("file").getInputStream();
		byte [] b=new byte[1024];
		int len=0;
		while ((len=in.read(b))!=-1) {
			fos.write(b, 0, len);
		}
		fos.close();
		in.close();
	}
	
	@RequestMapping("/index")
	public String index(HttpServletRequest req) throws IOException{
		return "index";
	}
	
}
