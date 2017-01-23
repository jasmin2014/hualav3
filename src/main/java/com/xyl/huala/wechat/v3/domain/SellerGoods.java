package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.xyl.huala.entity.HSellerGoods;

/**
 * @tag
 */
public class SellerGoods extends HSellerGoods implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4141797104422582666L;
	/**
	 * 商品描述信息
	 */
	private String goodsDesc;
	/**
	 * 商品ID
	 */
	private Long goodsId;

	/**
	 * 
	 */
	private Long skuId;

	/**
	 * 
	 */
	private String properties;

	/**
	 * 供应商ID
	 */
	private Long supplierId;

	/**
	 * 销量
	 */
	private Integer sales;

	/**
	 * 信息
	 */
	private Map<String, StringBuffer> information;

	// 保存规格信息
	private List<SellerSkuProp> sellerSkuPropList;

	/**
	 * 活动商品图片
	 */
	private String actImgUrl;
	/**
	 * 图片信息
	 */
	private List<String> imgUrlList;

	/**
	 * 供应商名字
	 */
	private String supplierName;
	
	/******************************以下活动信息**********************************************/

	/**
	 * 打折的折数 如：打9折 取值900
	 */
	private Long discount;
	/**
	 * 减价的价钱 单位：分  减40元 填4000
	 */
	private Long subtract;
	/**
	 * 活动图标
	 */
	private String actPicUrl;
	/**
	 * 活动名称
	 */
	private String actName;
	/******************************以上活动信息**********************************************/
	public Long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	public List<SellerSkuProp> getSellerSkuPropList() {
		return sellerSkuPropList;
	}

	public void setSellerSkuPropList(List<SellerSkuProp> sellerSkuPropList) {
		this.sellerSkuPropList = sellerSkuPropList;
	}

	public List<String> getImgUrlList() {
		return imgUrlList;
	}

	public void setImgUrlList(List<String> imgUrlList) {
		this.imgUrlList = imgUrlList;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}

	public Map<String, StringBuffer> getInformation() {
		return information;
	}

	public void setInformation(Map<String, StringBuffer> information) {
		this.information = information;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public Long getSkuId() {
		return skuId;
	}

	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	/**
	 * 商品描述信息
	 */
	public String getGoodsDesc() {
		return goodsDesc;
	}
	/**
	 * 商品描述信息
	 */
	public void setGoodsDesc(String goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	/**
	 * 获取活动价格，如果有活动信息，则销售价格需要减去活动的价格
	 * @return
	 */
	public Long getPrice(){
		if(this.getSubtract()!=null){
			return this.getSalePrice()-this.getSubtract();
		}else{
			return this.getSalePrice();
		}
	}
	
	
	/**
	 * sku的销售属性组合字符串（颜色，大小，等等，可通过类目API获取某类目下的销售属性）,格式是红色 XXL
	 */
	public String getPropName() {
		if (this.getProperties() != null) {
			StringBuffer sb = new StringBuffer();
			String[] props = this.getProperties().split(";");
			try {
				for (String prop : props) {
					sb.append(prop.split(":")[2] + ",");
				}
				sb.deleteCharAt(sb.lastIndexOf(","));
			} catch (Exception e) {
				return this.getProperties();
			}
			return sb.toString();
		}
		return this.getProperties();
	}

	public Long getDiscount() {
		return discount;
	}

	public void setDiscount(Long discount) {
		this.discount = discount;
	}

	public Long getSubtract() {
		return subtract;
	}

	public void setSubtract(Long subtract) {
		this.subtract = subtract;
	}

	public String getActPicUrl() {
		return actPicUrl;
	}

	public void setActPicUrl(String actPicUrl) {
		this.actPicUrl = actPicUrl;
	}

	public String getActName() {
		return actName;
	}

	public void setActName(String actName) {
		this.actName = actName;
	}

	public String getActImgUrl() {
		return actImgUrl;
	}

	public void setActImgUrl(String actImgUrl) {
		this.actImgUrl = actImgUrl;
	}

}
