package com.xyl.huala.wechat.v3.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Order;

/**
 * 订单处理
 * 
 * @author zxl0047
 *
 */
public interface V3OrderService {

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
	 * 确认下单，根据购物车获取店铺的商品信息
	 * 
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	public Order getOrderConfirm(Long userId, Long sellerId);

	/**
	 * 计算开店时间列表，用来供前台选择最佳收货时间
	 * 
	 * @param sellerId
	 * @return
	 */
	public Map<String, JSONObject> chooseDate(Long sellerId);

	/**
	 * 订单确认：检验订单数据的正确性，然后保存订单数据
	 * 
	 * @param sellerId
	 * @param order
	 *            前台传入的订单数据，后台在取一次，两次数据做检验，如果一致则通过
	 * @return
	 */
	public DataRet<String> checkOrder(Long sellerId, Order order);
	/**
	 * 订单处理：cancel取消订单：confirm：确认收货
	 */
	public String orderExec(Long orderId, String orderStatus);
	/**
	 * 更新订单未首单
	 * @param orderId
	 * @param orderSn
	 */
	public void updateOrderFirst(Long orderId, String orderSn);
}
