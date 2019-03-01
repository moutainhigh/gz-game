package com.walmart.mobile.checkout.vo;

import java.util.Date;

public class ItemLine {

	private String sku;// item_num

	private String customerStoreCode;

	private String itemName;// desc

	private String itemShortName;// pos

	private String price1;// retail_price

	private String price2;// wasPrice

	private String ean;// upc

	private String itemStatus;// stock_status

	private String unit;// unit

	private String placeOfOrigin;// manufacturer

	private String specification;// specification

	private String grade;// level

	private int packSize;// PACK_QTY

	private Date promoDateFrom;// priceStartDate

	private Date promoDateTo;// priceEndDate

	private String shelfCode;// horiz_facings_nbr

	private String displayLocation;// modular_code

	private int level1CategoryCode;// acct_dept

	private String price2Description;// prom_tab

	private String pricingStaff;// price_manager

	private String description;// texture

	private String price3Description;// promotionDescCn1

	private String price4Description;// promotionDescCn1

	private String price5Description;// promotionDescCn1

	private int promoFlag;// labeltype 1:原价 2：降价

	private String rsrvTxt5;// 标签打印时间:09/06/17

	private String rsrvTxt4;// 沃尔玛ITEM条码('40'||LPAD(itm.item_num,9,0) AS
							// item_barcode)

	private String qrCode;// 二维码

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getRsrvTxt4() {
		return rsrvTxt4;
	}

	public void setRsrvTxt4(String rsrvTxt4) {
		this.rsrvTxt4 = rsrvTxt4;
	}

	public String getRsrvTxt5() {
		return rsrvTxt5;
	}

	public void setRsrvTxt5(String rsrvTxt5) {
		this.rsrvTxt5 = rsrvTxt5;
	}

	public int getPromoFlag() {
		return promoFlag;
	}

	public void setPromoFlag(int promoFlag) {
		this.promoFlag = promoFlag;
	}

	public String getPrice3Description() {
		return price3Description;
	}

	public void setPrice3Description(String price3Description) {
		this.price3Description = price3Description;
	}

	public String getPrice4Description() {
		return price4Description;
	}

	public void setPrice4Description(String price4Description) {
		this.price4Description = price4Description;
	}

	public String getPrice5Description() {
		return price5Description;
	}

	public void setPrice5Description(String price5Description) {
		this.price5Description = price5Description;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getCustomerStoreCode() {
		return customerStoreCode;
	}

	public void setCustomerStoreCode(String customerStoreCode) {
		this.customerStoreCode = customerStoreCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getPrice1() {
		return price1;
	}

	public void setPrice1(String price1) {
		this.price1 = price1;
	}

	public String getItemShortName() {
		return itemShortName;
	}

	public void setItemShortName(String itemShortName) {
		this.itemShortName = itemShortName;
	}

	public String getEan() {
		return ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}

	public String getPrice2() {
		return price2;
	}

	public void setPrice2(String price2) {
		this.price2 = price2;
		if ("0.0".equals(price2)) {
			this.promoFlag = 1;
		} else {
			this.promoFlag = 2;
		}
	}

	public Date getPromoDateFrom() {
		return promoDateFrom;
	}

	public void setPromoDateFrom(Date promoDateFrom) {
		this.promoDateFrom = promoDateFrom;
	}

	public Date getPromoDateTo() {
		return promoDateTo;
	}

	public void setPromoDateTo(Date promoDateTo) {
		this.promoDateTo = promoDateTo;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getPlaceOfOrigin() {
		return placeOfOrigin;
	}

	public void setPlaceOfOrigin(String placeOfOrigin) {
		this.placeOfOrigin = placeOfOrigin;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public int getPackSize() {
		return packSize;
	}

	public void setPackSize(int packSize) {
		this.packSize = packSize;
	}

	public String getShelfCode() {
		return shelfCode;
	}

	public void setShelfCode(String shelfCode) {
		this.shelfCode = shelfCode;
	}

	public String getDisplayLocation() {
		return displayLocation;
	}

	public void setDisplayLocation(String displayLocation) {
		this.displayLocation = displayLocation;
	}

	public int getLevel1CategoryCode() {
		return level1CategoryCode;
	}

	public void setLevel1CategoryCode(int level1CategoryCode) {
		this.level1CategoryCode = level1CategoryCode;
	}

	public String getPrice2Description() {
		return price2Description;
	}

	public void setPrice2Description(String price2Description) {
		this.price2Description = price2Description;
	}

	public String getPricingStaff() {
		return pricingStaff;
	}

	public void setPricingStaff(String pricingStaff) {
		this.pricingStaff = pricingStaff;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
