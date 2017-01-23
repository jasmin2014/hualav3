package com.xyl.huala.wechat.v3.service;

import java.util.List;

import com.xyl.huala.wechat.v3.domain.CartInfo;
/**
 * 
 * @author leazx
 *
 */
public interface V3CartService {

	/**
	 * 增加购物车
	 * 
	 * @param userKey
	 *            sessionId或者userId
	 * @param sid
	 *            商铺信息
	 * @param num
	 *            商品数量
	 * @param goodSkuId
	 * @param type
	 *            0 为未登陆
	 * @return
	 */
	public CartInfo add(CartInfo cart);

	/**
	 * 减少购物车
	 * 
	 * @param userKey
	 *            sessionId或者userId
	 * @param sid
	 *            商铺信息
	 * @param num
	 *            商品数量
	 * @param goodSkuId
	 * @param type
	 *            0 为未登陆
	 * @return
	 */
	public CartInfo reduce(CartInfo cart);

	/**
	 * 获取购物车信息
	 * 
	 * @param sellerId
	 * @param userId
	 * @param skuId
	 * @return
	 */
	public CartInfo getCart(Long sellerId, String userId, Long skuId);

	/**
	 * 取得购物车列表
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({})
	public List<CartInfo> getList(String userId);

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
	public List<CartInfo> getList(Long userId, Long sellerId);

	/**
	 * 合并购物车，获取列表时，将购物车合并 <br>
	 * 更新购物车,将会话中的购物车信息更换成用户的ID，如果用户信息中没有，则直接修改userId，如果有则直接修改数量
	 * 
	 * @param sessionId
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({})
	public void mergeCart(String sessionId, String userId);

	/**
	 * 选中购物车
	 * 
	 * @param sellerId
	 *            店铺ID
	 * @param skuId
	 *            如果商品信息为空，则是全选或或全部取消
	 * @param userId
	 *            用户ID ，如果没登录则是会话ID
	 */
	public Boolean chooseCart(Long sellerId, Long skuId, String userId,
			Boolean choose);

	/**
	 * 清理购物车信息
	 * 
	 * @param userId
	 * @param sellerId
	 */
	public void clearCart(Long userId, Long sellerId);

	/**
	 * m删除购物车信息
	 * @param cart
	 */
	public void delete(CartInfo cart);
}
