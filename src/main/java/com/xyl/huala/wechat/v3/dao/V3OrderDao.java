package com.xyl.huala.wechat.v3.dao;

import java.util.List;

import com.xyl.huala.entity.ActUser;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.CartInfo;
import com.xyl.huala.wechat.v3.domain.Order;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;

public interface V3OrderDao {
	
	/**
	 * 获取订单信息
	 * 
	 * @param orderId
	 * @return
	 */
	public Order getOrderInfo(Long orderId);

	/**
	 * 获取订单详情
	 * 
	 * @param userId
	 * @param b
	 * @return
	 */
	public List<Order> getOrderList(Long userId, boolean b);

	/**
	 * 通过店铺ID与用户ID取得购物车列表
	 * 
	 * @param userId
	 *            用户ID
	 * @param sellerId
	 *            店铺ID
	 * @return
	 */
	@SuppressWarnings({})
	public List<CartInfo> getCartList(Long userId, Long sellerId);

	/**
	 * 獲取店铺信息
	 * 
	 * @param sellerId
	 * @return
	 */
	public Seller getSeller(Long sellerId);
	/**
	 * 查询红包信息
	 */
	public ActUser getRedCardById(Long redId);
	/**
	 * 查询商家的红包信息
	 */
	public List<ActCardExt> getUserCard(Long userId, Long sellerId);

	/**
	 * 确认下单处理
	 * 
	 * @param hOrder
	 * @param hOrderGoods
	 */
	public Order addOrder(Order orderInfo, Long sellerId);

	/**
	 * 查询商品详情
	 * @param skuId
	 * @return
	 */
	public SellerGoods getGoodsDetail(Long skuId);
	/**
	 * 查询某个订单是不是首单
	 */
	public boolean isFirstOrder(Long orderId, String orderSn);
}
