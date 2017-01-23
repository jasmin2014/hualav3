package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.xyl.huala.entity.ActGoods;
import com.xyl.huala.entity.ActInfo;

/**
 * 商品的活动信息 <br>
 * sellerGoods中的活动信息，如果有活动信息，则价格与活动图标需要显示
 * 
 * @author leazx
 *
 */
public class GoodsAct extends ActInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1860575606772401186L;
	
	/**
	 * 关联活动规则表的id.来源act_info主键
	 */
	private java.lang.Long actId;
	/**
	 * 关联商品表的id.来源h_seller_goods主键
	 */
	private java.lang.Long goodsId;
	/**
	 * 商品的sku_id 取值h_seller_sku_prop的id
	 */
	private java.lang.Long goodsSkuId;
	/**
	 * 商家id 取值h_seller的id
	 */
	private java.lang.Long sellerId;
	/**
	 * 打折的折数 如：打9折 取值900
	 */
	private java.lang.Long discount;
	/**
	 * 减价的价钱 单位：分  减40元 填4000
	 */
	private java.lang.Long subtract;
	
	/**
	 * 关联活动规则表的id.来源act_info主键
	 */
	public void setActId(java.lang.Long actId) {
		this.actId = actId;
	}
	/**
	 * 关联活动规则表的id.来源act_info主键
	 */
	public java.lang.Long getActId() {
		return this.actId;
	}
	/**
	 * 关联商品表的id.来源h_seller_goods主键
	 */
	public void setGoodsId(java.lang.Long goodsId) {
		this.goodsId = goodsId;
	}
	/**
	 * 关联商品表的id.来源h_seller_goods主键
	 */
	public java.lang.Long getGoodsId() {
		return this.goodsId;
	}
	/**
	 * 商品的sku_id 取值h_seller_sku_prop的id
	 */
	public void setGoodsSkuId(java.lang.Long goodsSkuId) {
		this.goodsSkuId = goodsSkuId;
	}
	/**
	 * 商品的sku_id 取值h_seller_sku_prop的id
	 */
	public java.lang.Long getGoodsSkuId() {
		return this.goodsSkuId;
	}
	/**
	 * 商家id 取值h_seller的id
	 */
	public void setSellerId(java.lang.Long sellerId) {
		this.sellerId = sellerId;
	}
	/**
	 * 商家id 取值h_seller的id
	 */
	public java.lang.Long getSellerId() {
		return this.sellerId;
	}
	/**
	 * 打折的折数 如：打9折 取值900
	 */
	public void setDiscount(java.lang.Long discount) {
		this.discount = discount;
	}
	/**
	 * 打折的折数 如：打9折 取值900
	 */
	public java.lang.Long getDiscount() {
		return this.discount;
	}
	/**
	 * 减价的价钱 单位：分  减40元 填4000
	 */
	public void setSubtract(java.lang.Long subtract) {
		this.subtract = subtract;
	}
	/**
	 * 减价的价钱 单位：分  减40元 填4000
	 */
	public java.lang.Long getSubtract() {
		return this.subtract;
	}
	
}
