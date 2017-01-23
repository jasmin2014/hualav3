package com.xyl.huala.wechat.v3.domain;

import java.util.Date;

/**
 * 购物车基本信息
 * 
 * @author leazx
 *
 */
public class CartInfo {
	/**
	 * 用户ID
	 */
	private String userId;
	/**
	 * 商品名称
	 */
	private String goodsName;
	/**
	 * 商家ID
	 */
	private Long sellerId;
	/**
	 * 商品SKUID
	 */
	private Long skuId;
	/**
	 * 商品ID
	 */
	private Long goodsId;

	/**
	 * 商品图片
	 */
	private String picUrl;

	/**
	 * 供应商ID
	 */
	private Long supplierId;
	/**
	 * 供应商名称
	 */
	private String supplierName;
	/**
	 * 商品的本店售价
	 */
	private Long salePrice;

	/**
	 * 购物车数据
	 */
	private Integer goodNum;
	/**
	 * 用户是否选中
	 */
	private Boolean choose=true;
	
	/**
	 * 购物车增加日期
	 */
	private Date addTime=new Date();
	
	/**
	 * 用户ID
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * 用户ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * 商家ID
	 */
	public Long getSellerId() {
		return sellerId;
	}

	/**
	 * 商家ID
	 */
	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	/**
	 * 商品SKUID
	 */
	public Long getSkuId() {
		return skuId;
	}

	/**
	 * 商品SKUID
	 */
	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	/**
	 * 商品图片
	 */
	public String getPicUrl() {
		return picUrl;
	}

	/**
	 * 商品图片
	 */
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	/**
	 * 供应商ID
	 */
	public Long getSupplierId() {
		return supplierId;
	}

	/**
	 * 供应商ID
	 */
	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	/**
	 * 商品的本店售价
	 */
	public Long getSalePrice() {
		return salePrice;
	}

	/**
	 * 商品的本店售价
	 */
	public void setSalePrice(Long salePrice) {
		this.salePrice = salePrice;
	}
	/**
	 * 购物车数据
	 */
	public Integer getGoodNum() {
		return goodNum;
	}
	/**
	 * 购物车数据
	 */
	public void setGoodNum(Integer goodNum) {
		this.goodNum = goodNum;
	}

	/**
	 * 商品名称
	 * @return
	 */
	public String getGoodsName() {
		return goodsName;
	}
	/**
	 * 商品名称
	 */
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	/**
	 * 供应商名称
	 */
	public String getSupplierName() {
		return supplierName;
	}
	/**
	 * 供应商名称
	 */
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	/**
	 * 用户是否选中
	 */
	public Boolean getChoose() {
		return choose;
	}
	/**
	 * 用户是否选中
	 */
	public void setChoose(Boolean choose) {
		this.choose = choose;
	}
	/**
	 * 购物车增加日期
	 */
	public Date getAddTime() {
		return addTime;
	}
	/**
	 * 购物车增加日期
	 */
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	/**
	 * 商品ID
	 */
	public Long getGoodsId() {
		return goodsId;
	}
	/**
	 * 商品ID
	 */
	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

}
