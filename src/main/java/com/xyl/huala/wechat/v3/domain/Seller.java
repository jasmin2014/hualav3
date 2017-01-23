package com.xyl.huala.wechat.v3.domain;

import java.util.List;
import com.xyl.huala.entity.HSeller;

/**   
 * @tag
 */
public class Seller extends HSeller{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4399341318732727138L;

	/**
	 * 优惠信息
	 */
	private List<String>  discountList;
	/**
	 * 供应商列表,包含自身ID
	 */
	private List<Long>  supplierList;
	/**
	 * 店铺评级
	 */
	private Integer level;

	/**
	 * 销量
	 */
	private Long sales;
	
	/**
	 * 起送费用
	 */
	private Long startAmount;
	
	/**
	 * 配送费用
	 */
	private Long deliveryAmount;
	
	/**
	 * 多少免配送
	 */
    private Long freeAmount ; 
   
    /**
     * 详细地址 地级市开始
     */
    private String addressDetail;
    /**
     * 营业执照 
     */    
    private String shopLicense;
    /**
     * 服务许可证
     */
    private String serveLicense;

    /**
     * 地理位置
     */
    private Double[] gps;
    /**
     * 配送范围，单位公里
     */
    private Integer sellerScope;
    /**
     * 开关店状态  0开店 1关店
     */
    private String sellerStatus;

    /**
     * 商品列表（不记进数据库），查询前台使用，mongo不保存
     */
    private List<SellerGoods> goodsList;
    
    public String getSellerStatus() {
		return sellerStatus == null ? "1" : sellerStatus;
	}

	public void setSellerStatus(String sellerStatus) {
		this.sellerStatus = sellerStatus;
	}

	/**
     * 优惠信息
     */
    private List<String> couponList; 

    /**
     * 是否在配送范围
     */
    private String isDelScope = "无法配送";

	public List<String> getDiscountList() {
		return discountList;
	}

	public void setDiscountList(List<String> discountList) {
		this.discountList = discountList;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Long getSales() {
		return sales;
	}

	public void setSales(Long sales) {
		this.sales = sales;
	}

	public Long getStartAmount() {
		return startAmount == null ? 0 : startAmount;
	}

	public void setStartAmount(Long startAmount) {
		this.startAmount = startAmount;
	}

	public Long getDeliveryAmount() {
		return deliveryAmount == null ? 0 : deliveryAmount;
	}

	public void setDeliveryAmount(Long deliveryAmount) {
		this.deliveryAmount = deliveryAmount;
	}

	public Long getFreeAmount() {
		return freeAmount == null ? 0l : freeAmount;
	}

	public void setFreeAmount(Long freeAmount) {
		this.freeAmount = freeAmount;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public String getShopLicense() {
		return shopLicense;
	}

	public void setShopLicense(String shopLicense) {
		this.shopLicense = shopLicense;
	}

	public String getServeLicense() {
		return serveLicense;
	}

	public void setServeLicense(String serveLicense) {
		this.serveLicense = serveLicense;
	}

	public String getIsDelScope() {
		return isDelScope;
	}

	public void setIsDelScope(String isDelScope) {
		this.isDelScope = isDelScope;
	}

	public List<String> getCouponList() {
		return couponList;
	}

	public void setCouponList(List<String> couponList) {
		this.couponList = couponList;
	}

	public Double[] getGps() {
		return gps;
	}

	public void setGps(Double[] gps) {
		this.gps = gps;
	}
	/**
	 * 供应商列表,包含自身ID
	 */
	public List<Long> getSupplierList() {
		return supplierList;
	}
	 /**
     * 配送范围，单位公里
     */
	public Integer getSellerScope() {
		return sellerScope;
	}
	 /**
     * 配送范围，单位公里
     */
	public void setSellerScope(Integer sellerScope) {
		this.sellerScope = sellerScope;
	}

	/**
	 * 供应商列表,包含自身ID
	 */
	public void setSupplierList(List<Long> supplierList) {
		this.supplierList = supplierList;
	}
	/**
     * 商品列表（不记进数据库），查询前台使用，mongo不保存
     */
	public List<SellerGoods> getGoodsList() {
		return goodsList;
	}
	/**
     * 商品列表（不记进数据库），查询前台使用，mongo不保存
     */
	public void setGoodsList(List<SellerGoods> goodsList) {
		this.goodsList = goodsList;
	}
}
