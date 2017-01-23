package com.xyl.huala.wechat.v3.service;

import java.awt.image.BufferedImage;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;

/**
 * 扫码直付的服务层接口
 */
public interface V3ScanCodeService {
	/**
	 * 根据店铺ID取出商家和活动信息
	 */
	public Seller getSellerActInfoById(Long sid);
	/**
	 * 扫码直接支付  生成订单信息
	 */
	public DataRet<String> addOrderInfo(Long sellerId, String payAmount, String amount, 
			String actCardId,String actUserId, Long userId, String isWxScan,String openId,String aliPayId);
	/**
	 * 根据订单id查询订单信息
	 */
	public HOrder getOrderInfoById(Long id);
	/**
	 * 生成支付二维码
	 * @param sellerId 店铺id
	 * @param content 二维码内容 
	 * @param logoUrl logo路径
	 */
	public BufferedImage genQrcode(Long sellerId, String content, String logoUrl);
	/**
	 * 查询店铺的名称
	 */
	public String getSellerNameById(Long sellerId);
	/**
	 * 查询用户的红包信息
	 * @param sellerId
	 * @param userId
	 */
	public ActCardExt getRedPackage(Long sellerId, Long userId);
	
}
