package com.zcc.game.vo;

import java.util.Date;

public class TaskVO {

	private Integer id;
	private String title;
	private String status;
	private String taskjf;
	private String userid;
	private String username;
	private String attribute2;
	private Date create_date;
	private Date lastup_date;
	
	private Integer tokenNum;
	
	
	public Integer getTokenNum() {
		return tokenNum;
	}
	public void setTokenNum(Integer tokenNum) {
		this.tokenNum = tokenNum;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTaskjf() {
		return taskjf;
	}
	public void setTaskjf(String taskjf) {
		this.taskjf = taskjf;
	}
	public String getAttribute2() {
		return attribute2;
	}
	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public Date getLastup_date() {
		return lastup_date;
	}
	public void setLastup_date(Date lastup_date) {
		this.lastup_date = lastup_date;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
}
