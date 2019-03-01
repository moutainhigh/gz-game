package com.walmart.mobile.checkout.vo;

import java.util.ArrayList;
import java.util.List;

public class BatchAndItemLine {

	private String storeCode;

	private String customerStoreCode;

	private String batchSize;

	private String batchNo;

	private List<ItemLine> items = new ArrayList<>();

	private String sign;

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getCustomerStoreCode() {
		return customerStoreCode;
	}

	public void setCustomerStoreCode(String customerStoreCode) {
		this.customerStoreCode = customerStoreCode;
	}

	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public List<ItemLine> getItems() {
		return items;
	}

	public void setItems(List<ItemLine> items) {
		this.items = items;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

}
