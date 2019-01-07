package com.zcc.game.vo;

import java.util.Date;

public class ReplyVO {

//	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
//	  `userid` VARCHAR(100) DEFAULT NULL COMMENT '用户ID',
//	  `useraccount` VARCHAR(100) DEFAULT NULL COMMENT '用户账号',
//	  `type` VARCHAR(100) DEFAULT NULL COMMENT '类型（后台回复，客户回复）',
//	  `content` VARCHAR(1000) DEFAULT NULL COMMENT '内容',
//	  `image1` VARCHAR(100) DEFAULT NULL COMMENT '图片1',
//	  `image2` VARCHAR(100) DEFAULT NULL COMMENT '图片2',
//	  `image3` VARCHAR(100) DEFAULT NULL COMMENT '图片3',
//	  `image4` VARCHAR(100) DEFAULT NULL COMMENT '图片4',
//	  `image5` VARCHAR(100) DEFAULT NULL COMMENT '图片5',
//	  `image6` VARCHAR(100) DEFAULT NULL COMMENT '图片6',
//	  `image7` VARCHAR(100) DEFAULT NULL COMMENT '图片7',
//	  `image8` VARCHAR(100) DEFAULT NULL COMMENT '图片8',
//	  `image9` VARCHAR(100) DEFAULT NULL COMMENT '图片9',
//	  `remark` VARCHAR(100) DEFAULT NULL COMMENT '备注',
//	  `create_date` TIMESTAMP NULL DEFAULT NULL COMMENT '创建时间',
	  private Integer id;
	  private String userid;
	  private String content;
	  private String type;
	  private String image1;
	  private String image2;
	  private String image3;
	  private String image4;
	  private String image5;
	  private String image6;
	  private String image7;
	  private String image8;
	  private String image9;
	  private String remark;
	  private Date create_date;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getImage1() {
		return image1;
	}
	public void setImage1(String image1) {
		this.image1 = image1;
	}
	public String getImage2() {
		return image2;
	}
	public void setImage2(String image2) {
		this.image2 = image2;
	}
	public String getImage3() {
		return image3;
	}
	public void setImage3(String image3) {
		this.image3 = image3;
	}
	public String getImage4() {
		return image4;
	}
	public void setImage4(String image4) {
		this.image4 = image4;
	}
	public String getImage5() {
		return image5;
	}
	public void setImage5(String image5) {
		this.image5 = image5;
	}
	public String getImage6() {
		return image6;
	}
	public void setImage6(String image6) {
		this.image6 = image6;
	}
	public String getImage7() {
		return image7;
	}
	public void setImage7(String image7) {
		this.image7 = image7;
	}
	public String getImage8() {
		return image8;
	}
	public void setImage8(String image8) {
		this.image8 = image8;
	}
	public String getImage9() {
		return image9;
	}
	public void setImage9(String image9) {
		this.image9 = image9;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	  
}
