package com.xyl.huala.wechat.v3.domain;

import com.xyl.huala.domain.Gps;

public class Place extends Gps{

	/**
	 * 
	 */
	private static final long serialVersionUID = 931010589005192955L;
	public Place(Double lat, Double lng, String address) {
		super(lat,lng);
		this.setAddress(address);
	}
	public Place(){
		
	}
	/**
	 * 地址
	 */
	private String address;
	/**
	 * 标志性建筑
	 */
	private String signBuilding;
	/**
	 * 店铺ID
	 */
	private Long sellerId;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getSignBuilding() {
		return signBuilding;
	}
	public void setSignBuilding(String signBuilding) {
		this.signBuilding = signBuilding;
	}
	public Long getSellerId() {
		return sellerId;
	}
	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}
	
	
	
}
