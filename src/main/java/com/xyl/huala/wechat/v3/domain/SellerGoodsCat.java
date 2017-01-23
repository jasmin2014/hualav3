package com.xyl.huala.wechat.v3.domain;

import java.io.Serializable;
import java.util.List;

import com.xyl.huala.entity.HSellerGoodsCat;

/**
 * @tag
 */
public class SellerGoodsCat extends HSellerGoodsCat implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8043808141421728045L;

	/**
	 * 此类下有多少商品
	 */
	private Integer goodsNum;

	/**
	 * 此类下有多少分类
	 */
	private Integer catNum;

	/**
	 * 此分类的子类目
	 */
	List<SellerGoodsCat> subCatList;

	/**
	 * 是否是活动类目
	 */
	private boolean isAct;

	public Integer getCatNum() {
		return catNum;
	}

	public void setCatNum(Integer catNum) {
		this.catNum = catNum;
	}

	public Integer getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}

	public List<SellerGoodsCat> getSubCatList() {
		return subCatList;
	}

	public void setSubCatList(List<SellerGoodsCat> subCatList) {
		this.subCatList = subCatList;
	}

	/**
	 * 是否是活动类目
	 */
	public boolean isAct() {
		return isAct;
	}

	/**
	 * 是否是活动类目
	 */
	public void setAct(boolean isAct) {
		this.isAct = isAct;
	}
}
