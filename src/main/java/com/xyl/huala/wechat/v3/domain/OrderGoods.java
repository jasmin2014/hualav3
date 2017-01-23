package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;

import com.xyl.huala.entity.HOrderGoods;
import com.xyl.huala.enums.OrderLogEnum;

/**   
 * @tag
 */
@SuppressWarnings("serial")
public class OrderGoods extends HOrderGoods implements Serializable {
	
	/**
	 * 供应商名称
	 */
	private String supplierName;
	/**
	 * 订单商品的状态
	 */
	private String orderGoodsStatus;
	public OrderGoods() {
		super();
	}

	public OrderGoods(Long id) {
		super.setId(id); 
	}
	
	public OrderGoods(Long orderId, String goodsSn) {
		super.setOrderId(orderId);
		super.setGoodsSn(goodsSn);
	}

	public String getSimpleAttr(){
		if(super.getGoodsAttr() != null){
			StringBuffer sb  = new StringBuffer();
			String[] props = super.getGoodsAttr().split(";");
			try{
				for(String prop : props){
					sb.append(prop.split(":")[2]+",");
				}
				sb.deleteCharAt(sb.lastIndexOf(","));
			}catch(Exception e){
			}
			return sb.toString();
		}
		return super.getGoodsAttr();
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
	 * 订单商品的状态
	 */
	public String getOrderGoodsStatus() {
		
		return this.orderGoodsStatus;
	}
	
	/**
	 * 订单商品的状态:文本显示
	 */
	public String getOrderGoodsStatusStr() {
		return OrderLogEnum.getValue(this.orderGoodsStatus);
	}
	/**
	 * 订单商品的状态
	 */
	public void setOrderGoodsStatus(String orderGoodsStatus) {
		this.orderGoodsStatus = orderGoodsStatus;
	}

	
	
}
