package com.xyl.huala.wechat.v3.util;

import java.io.Serializable;

import com.xyl.huala.domain.Gps;

@SuppressWarnings("serial")
public class BaiduMap implements Serializable {
	
	/**
	 * 地址名称
	 */
	private String name;
	
	/**
	 * 经纬度
	 */
	private Gps location;
	/**
	 * 
	 */
	private String uid;
	/**
	 * 城市名称
	 */
	private String city;
	/**
	 * 区域名称
	 */
	private String district;
	/**
	 * 
	 */
	private String business;
	/**
	 * 城市Id
	 */
	private String cityid;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Gps getLocation() {
		return location;
	}
	public void setLocation(Gps location) {
		this.location = location;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public String getCityid() {
		return cityid;
	}
	public void setCityid(String cityid) {
		this.cityid = cityid;
	}
	
	
	
}
