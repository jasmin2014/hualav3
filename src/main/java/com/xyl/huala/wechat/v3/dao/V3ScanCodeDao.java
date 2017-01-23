package com.xyl.huala.wechat.v3.dao;

import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HSeller;
import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.wechat.v3.domain.Seller;

/**
 * 扫码直付的DAO服务
 */
public interface V3ScanCodeDao {
	/**
	 * 根据sid获取店铺名称
	 */
	public Seller getSellerNameById(Long sid);
	/**
	 * 根据订单id查询相应的订单信息
	 * @param id
	 * @return
	 */
	public HOrder getOrderInfoById(Long id);
	/**
	 * 根据用户id查询用户信息
	 */
	public HSsoUser getUserInfoByMobile(Long userId);
	/**
	 * 获取商家的地址信息
	 */
	public HSeller getSellerAddressById(Long sellerId);
	/**
	 * 获取用户红包信息
	 */
	public ActUser getNoUseCard(Long actCardId, Long sellerId, Long userId);
}
