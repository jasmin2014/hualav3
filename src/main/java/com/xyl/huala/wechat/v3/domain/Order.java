package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HOrderCoupon;
import com.xyl.huala.enums.OrderLogEnum;

/**
 * 订单信息：里面饮食基础信息，商品明细信息，优惠信息
 * 
 * @author zxl0047
 */
public class Order extends HOrder implements Serializable {
	private static final long serialVersionUID = -207913537406747723L;
	/**
	 * 地址的id
	 */
	private Long addressId;
	/**
	 * 红包的id
	 */
	private Long redId;

	/**
	 * 订单明细信息
	 */
	private List<OrderGoods> orderGoods;
	/**
	 * 订单优惠信息
	 */
	private List<HOrderCoupon> orderCoupons;

	/**
	 * 红包信息
	 */
	private List<ActCardExt> actCart;
	/**
	 * 配送方式：0自配，1自提，2第三方配送
	 */
	public String getShippingTypeStr() {
		if("0".equals(super.getShippingType())) return "商家配送";
		else if("1".equals(super.getShippingType())) return "自提";
		return "第三方配送";		
	}

	/**
	 * 取得订单状态描述
	 */
	public String getOrderStatusStr() {
		return OrderLogEnum.getValue(this.getOrderStatus());
	}
	

	/**
	 * 设置商品明细
	 * 
	 * @return
	 */
	public List<OrderGoods> getOrderGoods() {
		if (orderGoods == null) {
			orderGoods = new ArrayList<OrderGoods>();
		}
		return orderGoods;
	}

	/**
	 * 添加商品明细
	 * 
	 * @return
	 */
	public List<OrderGoods> addOrderGoods(OrderGoods orderGoods) {
		getOrderGoods();
		this.orderGoods.add(orderGoods);
		return this.orderGoods;
	}

	public void setOrderGoods(List<OrderGoods> orderGoods) {
		this.orderGoods = orderGoods;
	}

	/**
	 * 设置优惠信息
	 * 
	 * @param hOrderCoupon
	 * @return
	 */
	public List<HOrderCoupon> getOrderCoupons() {
		if (orderCoupons == null) {
			orderCoupons = new ArrayList<HOrderCoupon>();
		}
		return orderCoupons;
	}

	
	/**
	 * 添加优惠信息
	 * 
	 * @param hOrderCoupon
	 * @return
	 */
	public List<HOrderCoupon> addOrderCoupons(HOrderCoupon hOrderCoupon) {
		this.getOrderCoupons();
		orderCoupons.add(hOrderCoupon);
		return orderCoupons;
	}

	public void setOrderCoupons(List<HOrderCoupon> orderCoupons) {
		this.orderCoupons = orderCoupons;
	}

	public Long getRedId() {
		return redId;
	}

	public void setRedId(Long redId) {
		this.redId = redId;
	}

	public Long getAddressId() {
		return addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}
	/**
	 * 红包信息
	 */
	public List<ActCardExt> getActCart() {
		return actCart;
	}
	/**
	 * 红包信息
	 */
	public void setActCart(List<ActCardExt> actCart) {
		this.actCart = actCart;
	}
	/**
	 * 获取优惠信息的总金额(通过getOrderCoupons计算累计金额)
	 */
	public Long getCouponsAmount(){
		Long amount=0L;
		for(HOrderCoupon h:getOrderCoupons()){
			amount+=h.getPrice();
		}
		return amount;
	}
	/**
	 * 计算商品总金额
	 * @return
	 */
	public Long caclGoodsAmount(){
		Long amount=0L;
		for(OrderGoods h:this.getOrderGoods()){
			amount+=h.getSalePrice()*h.getGoodsNumber();
		}
		return amount;
	}
	/**
	 * 取得订单最终金额
	 * @return
	 */
	public Long caclOrderAmount(){
		Long oa = this.getGoodAmount()+this.getShippingAmount()-this.getCouponsAmount();
		if(oa < 0) return 0l;
		return oa;
	}
}
