package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 商品的活动信息 <br>
 * sellerGoods中的活动信息，如果有活动信息，则价格与活动图标需要显示
 * 
 * @author leazx
 *
 */
public class GoodsActGroup implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4725816595010601946L;
	/**
	 * 活动ID
	 */
	private Long actId;
	/**
	 * 活动名称
	 */
	private String name;
	/**
	 * 活动图标
	 */
	private String imgUrl;
	
	/**
	 * 活动导航主图
	 */
	private String picImg;
	
	/**
	 * 商品列表
	 */
	public List<SellerGoods> goodsList;
	
	
	
	public Long getActId() {
		return actId;
	}
	public void setActId(Long actId) {
		this.actId = actId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public List<SellerGoods> getGoodsList() {
		return goodsList;
	}
	public void setGoodsList(List<SellerGoods> goodsList) {
		this.goodsList = goodsList;
	}
	public String getPicImg() {
		return picImg;
	}
	public void setPicImg(String picImg) {
		this.picImg = picImg;
	}
	
	
}
