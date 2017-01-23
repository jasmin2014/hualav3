package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import com.xyl.huala.entity.ActUser;

/**
 * 红包扩展实体信息
 */
public class ActCardExt extends ActUser implements Serializable {
	private static final long serialVersionUID = -2921448704801218699L;
	/**
	 * 红包名称
	 */
	private String name;

	/**
	 * 满减券是否能使用 0能使用 1不能使用
	 */
	private int isUse;

	/**
	 * 商店名称
	 */
	private String sellerName;
	/**
	 * 优惠券是否超时 0否 1是
	 */
	private String isInvalid;
	/**
	 * 失效时间 单位是天
	 */
	private Integer validDays;
	/**
	 * 平台承担的金额 单位:分
	 */
	private Long platformPrice;

	/**
	 * 活动说明
	 */
	private java.lang.String content;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsUse() {
		return isUse;
	}

	public void setIsUse(int isUse) {
		this.isUse = isUse;
	}

	public String getIsInvalid() {
		return isInvalid;
	}

	public void setIsInvalid(String isInvalid) {
		this.isInvalid = isInvalid;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public Integer getValidDays() {
		return validDays;
	}

	public void setValidDays(Integer validDays) {
		this.validDays = validDays;
	}
	/**
	 * 平台承担的金额 单位:分
	 */
	public Long getPlatformPrice() {
		return platformPrice;
	}
	/**
	 * 平台承担的金额 单位:分
	 */
	public void setPlatformPrice(Long platformPrice) {
		this.platformPrice = platformPrice;
	}

	public java.lang.String getContent() {
		return content;
	}

	public void setContent(java.lang.String content) {
		this.content = content;
	}

}
