package com.walmart.mobile.checkout.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.walmart.mobile.checkout.entity.document.BaseDocument;

@JsonInclude(Include.NON_NULL)
@Document(collection = "store_elabel_config")
public class StoreElabelConfig extends BaseDocument<BigInteger> {

	private static final long serialVersionUID = 1L;

	@Field("store_code")
	private Integer storeCode;
	@Field("customer_code")
	private String customerCode;
	@Field("vendor_name")
	private String vendorName;
	@Field("vendor_type")
	private Integer vendorType;
	@Field("dept_list")
	private List<Integer> deptList = new ArrayList<>();
	@Field("token")
	private String token;
	@Field("url")
	private String url;
	@Field("write_log_flag")
	private Boolean writeLogFlag = true;

	public Integer getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(Integer storeCode) {
		this.storeCode = storeCode;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public Integer getVendorType() {
		return vendorType;
	}

	public void setVendorType(Integer vendorType) {
		this.vendorType = vendorType;
	}

	public List<Integer> getDeptList() {
		return deptList;
	}

	public void setDeptList(List<Integer> deptList) {
		this.deptList = deptList;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getWriteLogFlag() {
		return writeLogFlag;
	}

	public void setWriteLogFlag(Boolean writeLogFlag) {
		this.writeLogFlag = writeLogFlag;
	}
	
}