package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HOrderCoupon;
import com.xyl.huala.entity.HOrderGoods;
import com.xyl.huala.entity.HSeller;

/**
 * 购物车信息
 * 
 * @author leazx
 *
 */
public class CartItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1169129088904591563L;

	/**
	 * 店铺入口ID
	 */
	private Long sellerId;

	/**
	 * 用户ID（如果没登录则为会话ID）
	 */
	private String userId;
	/**
	 * 卖家名
	 */
	private String name;
	/**
	 * 起送金额
	 */
	private Long startAmount;
    /**
     * 开关店状态  0开店 1关店
     */
    private String sellerStatus;
	/**
	 * 购物车信息列表
	 */
	private List<CartInfo> cartList;
	/**
	 * 是否所有商品都已选中
	 */
	private Boolean chooseAll=true;
	/**
	 * 购物车已选商品金额
	 */
	private Long total=0L;
	/**
	 * 店铺入口ID
	 */
	public Long getSellerId() {
		return sellerId;
	}

	/**
	 * 店铺入口ID
	 */
	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	/**
	 * 用户ID（如果没登录则为会话ID）
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * 用户ID（如果没登录则为会话ID）
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * 卖家名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 卖家名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 购物车信息列表
	 */
	public List<CartInfo> getCartList() {
		return cartList;
	}

	/**
	 * 购物车信息列表
	 */
	public void setCartList(List<CartInfo> cartList) {
		this.cartList = cartList;
	}
	/**
	 * 是否所有商品都已选中
	 */
	public Boolean getChooseAll() {
		return chooseAll;
	}
	/**
	 * 是否所有商品都已选中
	 */
	public void setChooseAll(Boolean chooseAll) {
		this.chooseAll = chooseAll;
	}
	/**
	 * 购物车已选商品金额
	 */
	public Long getTotal() {
		return total;
	}
	/**
	 * 购物车已选商品金额
	 */
	public void setTotal(Long total) {
		this.total = total;
	}

	public void addCart(CartInfo cartInfo) {
		if (this.cartList == null)
			this.cartList = new ArrayList<>();
		this.cartList.add(cartInfo);
	}
	/**
	 * 起送金额
	 */
	public Long getStartAmount() {
		return startAmount;
	}
	/**
	 * 起送金额
	 */
	public void setStartAmount(Long startAmount) {
		this.startAmount = startAmount;
	}
	/**
     * 开关店状态  0开店 1关店
     */
	public String getSellerStatus() {
		return sellerStatus;
	}
	/**
     * 开关店状态  0开店 1关店
     */
	public void setSellerStatus(String sellerStatus) {
		this.sellerStatus = sellerStatus;
	}
	
}
