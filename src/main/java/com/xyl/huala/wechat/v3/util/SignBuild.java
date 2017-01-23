package com.xyl.huala.wechat.v3.util;

import java.io.Serializable;

import com.xyl.huala.domain.Gps;


/**
 * 百度地图标志性建筑
 * 
 * @author zxl0047
 *
 */
public class SignBuild implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5183968029567229747L;
	private String address;
	private Gps location;
	private String name;
	private DetaiInfo detail_info;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Gps getLocation() {
		return location;
	}

	public void setLocation(Gps location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DetaiInfo getDetail_info() {
		return detail_info;
	}

	public void setDetail_info(DetaiInfo detail_info) {
		this.detail_info = detail_info;
	}

	public class DetaiInfo{
		private int distance;
		private String tag;
		
		public int getDistance() {
			return distance;
		}
		public void setDistance(int distance) {
			this.distance = distance;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
	}
}