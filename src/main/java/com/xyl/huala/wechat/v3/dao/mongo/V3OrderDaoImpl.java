package com.xyl.huala.wechat.v3.dao.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.xyl.huala.utils.DateUtils;
import com.xyl.huala.utils.EmojiFilter;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HOrderCoupon;
import com.xyl.huala.entity.HOrderGoods;
import com.xyl.huala.enums.OrderLogEnum;
import com.xyl.huala.wechat.v3.dao.V3OrderDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.CartInfo;
import com.xyl.huala.wechat.v3.domain.Order;
import com.xyl.huala.wechat.v3.domain.OrderGoods;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;

@Repository
@Profile("mongo")
public class V3OrderDaoImpl implements V3OrderDao {
	private static Logger log = Logger.getLogger(V3OrderDaoImpl.class);
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private MongoTemplate mongo;

	/**
	 * 获取订单信息
	 * 
	 * @param orderId
	 * @return
	 */
	public Order getOrderInfo(Long orderId) {
		Order o = jdbcDao.queryOneBySQL("select a.*,b.name email from h_order a left join h_seller b on a.referer=b.id where a.id=? ",
				Order.class, orderId);
		List<OrderGoods> goods = jdbcDao.queryBySQL(
				"select * from h_order_goods where order_id=?",
				OrderGoods.class, o.getId());
		o.setOrderGoods(goods);
		return o;
	}

	/**
	 * 获取订单详情
	 * 
	 * @param userId
	 * @param b
	 * @return
	 */
	public List<Order> getOrderList(Long userId, boolean b) {
		List<Order> orderList = jdbcDao.queryBySQL(
				"select a.*,b.name email from h_order a left join h_seller b on a.referer=b.id where a.user_id=? order by add_time desc limit 10", Order.class,userId);
		for (Order o : orderList) {
			List<OrderGoods> goods = jdbcDao.queryBySQL(
					"select * from h_order_goods where order_id=?",
					OrderGoods.class, o.getId());
			o.setOrderGoods(goods);
		}
		return orderList;
	}

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
	public List<CartInfo> getCartList(Long userId, Long sellerId) {
		Criteria where = Criteria.where("userId").is("" + userId)
				.and("sellerId").is(sellerId).and("choose").is(true);
		Query query = Query.query(where);
		List<CartInfo> cartList = mongo.find(query, CartInfo.class);
		return cartList;
	}

	/**
	 * 获取商品信息
	 * 
	 * @param sellerId
	 * @param skuId
	 */
	public OrderGoods getGoodInfo(Long sellerId, Long skuId) {
		OrderGoods og = new OrderGoods();
		SellerGoods sg = mongo.findOne(
				Query.query(Criteria.where("skuId").is(skuId)),
				SellerGoods.class);
		og.setSellerId(sellerId);
		og.setSalePrice(sg.getPrice());
		og.setSupplierId(sg.getSupplierId());
		og.setSupplierName(sg.getSupplierName());
		og.setGoodsName(sg.getTitle());
		og.setPicUrl(sg.getPicUrl());
		og.setSellerGoodsId(sg.getGoodsId());
		og.setSellerSkuId(sg.getSkuId());
		og.setGoodsSn(sg.getGoodsSn());
		og.setGoodsAttr(sg.getProperties());
		return og;
	}

	/**
	 * 獲取店铺信息
	 * 
	 * @param sellerId
	 * @return
	 */
	public Seller getSeller(Long sellerId) {
		Query query = Query.query(Criteria.where("id").is(sellerId));
		Seller seller = mongo.findOne(query, Seller.class);
		return seller;
	}
	/**
	 * 查询红包信息
	 */
	public ActCardExt getRedCardById(Long redId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT a.id, a.balance, c.name, a.type, b.valid_days, b.platform_price FROM  act_user a,")
           .append(" act_card b, act_info c WHERE a.act_cart_id = b.id AND b.act_id = c.id AND a.id = ?");
		return jdbcDao.queryOneBySQL(sql.toString(), ActCardExt.class, redId);
	}
	/**
	 * 根据购物车信息获取商品列表
	 * 
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	public List<OrderGoods> getOrderGoodsList(Long userId, Long sellerId) {
		List<CartInfo> cartList = this.getCartList(userId, sellerId);
		Long goodsAmount = 0L;
		List<OrderGoods> goodsList = new ArrayList<>();
		for (CartInfo cart : cartList) {
			OrderGoods og = this.getGoodInfo(sellerId, cart.getSkuId());
			og.setGoodsNumber(cart.getGoodNum());
			goodsAmount += og.getSalePrice() * og.getGoodsNumber();
			goodsList.add(og);
		}
		return goodsList;
	}
	
	/**
	 * 查询商家的红包信息
	 */
	private static final String actCart= "SELECT xx.*, hs.NAME AS sellerName FROM ( SELECT a.id,a.balance,a.end_time,a.type,a.start_day,"
			+ " c. NAME, b.valid_days, b.seller_id FROM act_user a, act_card b, act_info c WHERE a.act_cart_id = "
			+ "b.id AND b.act_id = c.id AND a. STATUS = '0' AND c. STATUS = '0' AND now() BETWEEN a.start_day AND a.end_time AND"
			+ " b.is_delete = '0'  AND a.user_id = ? AND ( b.seller_id = ? or b.seller_id = 0 ) ) "
			+ "xx LEFT JOIN h_seller hs ON xx.seller_id = hs.id order by xx.balance,xx.end_time";
	public List<ActCardExt> getUserCard(Long userId, Long sellerId) {
		//log.info(actCart);
		List<ActCardExt> queryBySQL = jdbcDao.queryBySQL(actCart, ActCardExt.class, userId, sellerId);
		return queryBySQL;
	}

	/**
	 * 确认下单处理
	 */
	public Order addOrder(Order orderInfo, Long sellerId) {
		// 将orderInfo对象中的信息保存至save对象中
		if (orderInfo.getBestTime() == null) {
			orderInfo.setBestTime(new Date());
		}
		HOrder order = new HOrder();
		BeanUtils.copyProperties(orderInfo, order);
		order.setPostscript(orderInfo.getPostscript());
		order.setOrderSn(orderInfo.getOrderSn());
		order.setDiscountAmount(orderInfo.getCouponsAmount());
		order.setOrderAmount(orderInfo.getGoodAmount()+orderInfo.getShippingAmount()-orderInfo.getDiscountAmount());
		order.setAddTime(new Date());
		order.setOrderStatus(OrderLogEnum.NO_PAY.getOptKey());
		order.setPayType("wx");
		order.setPostscript(order.getPostscript()==null?"无":order.getPostscript());
		order.setReferer("" + sellerId);
		order.setPostscript(EmojiFilter.filterEmoji(order.getPostscript()+"  "));
		order.setSurplusAmount(0l);
		Date bestTime = orderInfo.getBestTime();
		int hour = DateUtils.getQuotHours(bestTime, new Date());
		if (hour > 2) {
			order.setOrderType("2");
		} else {
			order.setOrderType("1");
		}
		if (order.getPostscript() == null || "".equals(order.getPostscript().trim())) {
			order.setPostscript("无");
		}
		// 最佳收货时间是明天 最佳配送时间
		List<OrderGoods> orderGoods = orderInfo.getOrderGoods();
		List<HOrderCoupon> orderCoupons = orderInfo.getOrderCoupons();
		Long id = jdbcDao.insert(order);
		orderInfo.setId(id);
		log.info(JSON.toJSONString(orderInfo));
		if (orderGoods != null && orderGoods.size() > 0) {
			for (HOrderGoods og : orderGoods) {
				HOrderGoods ogs = new HOrderGoods();
				BeanUtils.copyProperties(og, ogs);
				ogs.setOrderId(id);
				ogs.setSellerId(sellerId);
				jdbcDao.insert(ogs);
			}
		}
		if (orderCoupons != null && orderCoupons.size() > 0) {
			for (HOrderCoupon oc : orderCoupons) {
				oc.setOrderId(id);
				jdbcDao.insert(oc);
			}
		}
		return orderInfo;
	}

	@Override
	public SellerGoods getGoodsDetail(Long skuId) {
		return null;
	}

	@Override
	public boolean isFirstOrder(Long orderId, String orderSn) {
		return false;
	}
}
