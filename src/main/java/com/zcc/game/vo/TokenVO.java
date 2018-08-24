/**
 * 版权：zcc
 * 作者：c0z00k8
 * @data 2018年8月24日
 */
package com.zcc.game.vo;

import java.util.Date;

/**
 * @author c0z00k8
 *
 */
public class TokenVO {

	private Integer id;
	private String userid;
	private String username;
	private String account;
	private String tokennum;
	private String changetype;
	private Date create_date;
	private Date lastup_date;
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
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getTokennum() {
		return tokennum;
	}
	public void setTokennum(String tokennum) {
		this.tokennum = tokennum;
	}
	public String getChangetype() {
		return changetype;
	}
	public void setChangetype(String changetype) {
		this.changetype = changetype;
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
