/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月23日
 */
package com.zcc.game.vo;

import java.util.Date;

/**
 * @author c0z00k8
 *
 */
public class MessageVO {

	private Integer id;
	private String content;
	private String userid;
	private String username;
	private String account;
	private String telephone;
	private Date create_date;
	private Date lastup_date;
	
	private String reply;
	private String reply2;
	
	
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public String getReply2() {
		return reply2;
	}
	public void setReply2(String reply2) {
		this.reply2 = reply2;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
	
}

