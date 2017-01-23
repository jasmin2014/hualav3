package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.xyl.huala.entity.HSellerGoods;

/**
 * @tag
 */
public class SellerIncomeDetail  implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5913321540937107505L;
	/**
	 * 商品描述信息
	 */
	private String sellerId;
	/**
	 * 付款时间
	 */
	private String addTime;

	/**
	 * 金额
	 */
	private String amount;

	/**
	 * 订单sn
	 */
	private String orderSn;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 商家名称
	 */
	private String name;

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getOrderSn() {
		return orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
