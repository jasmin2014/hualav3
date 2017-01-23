package com.xyl.huala.wechat.v3.dao.mysql;

import java.util.Date;
import java.util.List;

import com.xyl.huala.utils.DateUtils;
import com.xyl.huala.utils.EmojiFilter;
import org.apache.commons.lang.StringUtils;
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
import com.xyl.huala.entity.ActUser;
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
@Profile("mysql")
public class V3OrderDaoImpl implements V3OrderDao {
	private static Logger log = Logger.getLogger(V3OrderDaoImpl.class);
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	MongoTemplate mongo;

	/**
	 * 获取订单信息
	 *
	 * @param orderId
	 * @return
	 */
	public Order getOrderInfo(Long orderId) {
		String sql = "select a.*,b.name email from h_order a left join h_seller b on a.referer=b.id where a.id=? ";
		String sql2 = "select a.*,b.order_status order_goods_status,c.`name` supplier_name "
			+ "from h_order_goods a INNER JOIN	h_seller c on a.supplier_id=c.id LEFT JOIN h_seller_order_details b  on a.id=b.order_goods_id "
			+ "where  a.order_id=?";
		Order o = jdbcDao.queryOneBySQL(sql, Order.class, orderId);
		List<OrderGoods> goods = jdbcDao.queryBySQL(sql2, OrderGoods.class,
			o.getId());
		o.setOrderGoods(goods);
		return o;
	}

	/**
	 * 获取订单详情
	 *
	 * @param userId
	 * @param isHistory
	 * @return
	 */
	public List<Order> getOrderList(Long userId, boolean isHistory) {
		String sql = "select a.*,b.name email from h_order a left join h_seller b on a.referer=b.id where a.user_id=? and a.order_status";
		if (isHistory) {//通过not来控制是否为历史订单
			sql += " not ";
		}
		sql += " in ('no_pay','paying','have_pay','shipping','wait_take','return_goods','shipping_done') and a.order_status <> 'close' order by add_time desc ";
		String sql2 = "select * from h_order_goods where order_id=?";
		List<Order> orderList = jdbcDao.queryBySQL(sql, Order.class, userId);
		for (Order o : orderList) {
			List<OrderGoods> goods = jdbcDao.queryBySQL(sql2, OrderGoods.class,
				o.getId());
			o.setOrderGoods(goods);
		}
		return orderList;
	}

	/**
	 * 通过店铺ID与用户ID取得购物车列表
	 *
	 * @param userId   用户ID
	 * @param sellerId 店铺ID
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
	 * 獲取店铺信息
	 *
	 * @param sellerId
	 * @return
	 */
	private static final String sql = "select a.*,b.delivery_amount,b.avoid_amount free_amount from h_seller a left join v_seller_ext b on a.id=b.seller_id where id=?";
	private static final String sql2 = "select data_value from h_seller_extends where group_key='basic_pro' and data_key = 'seller_status'  and seller_id=?";

	public Seller getSeller(Long sellerId) {
		Seller seller = jdbcDao.queryOneBySQL(sql, Seller.class, sellerId);
		String queryOneBySQL = jdbcDao.queryOneBySQL(sql2, String.class, sellerId);
		//是否开店
		seller.setSellerStatus(queryOneBySQL);
		seller.setIsDelete(null);
		return seller;
	}

	/**
	 * 查询红包信息
	 */
	public ActUser getRedCardById(Long actUserId) {
		String sql = " SELECT * FROM  act_user a WHERE a.id = ?";
		return jdbcDao.queryOneBySQL(sql.toString(), ActUser.class, actUserId);
	}

	/**
	 * 查询商家的红包信息
	 */
	private static final String actCart = "select a.*,(select content from act_card where id = a.act_cart_id) as content from act_user a where a.user_id=? and  now() between a.start_day and a.end_time and a.status in('0') and (seller_id = ? or seller_id = 0)";

	public List<ActCardExt> getUserCard(Long userId, Long sellerId) {
		// log.info(actCart);
		List<ActCardExt> queryBySQL = jdbcDao.queryBySQL(actCart,
			ActCardExt.class, userId, sellerId);
		return queryBySQL;
	}


	/**
	 * 确认下单处理
	 * @param orderInfo
	 * @param sellerId
	 * @return
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
		order.setOrderAmount(orderInfo.caclOrderAmount());
		order.setAddTime(new Date());
		order.setOrderStatus(OrderLogEnum.NO_PAY.getOptKey());
		order.setPayType("wx");
		order.setPostscript(order.getPostscript() == null ? "无" : order
			.getPostscript());
		order.setReferer("" + sellerId);
		order.setSellerId(sellerId);
		order.setPostscript(EmojiFilter.filterEmoji(order.getPostscript()
			+ "  "));
		order.setSurplusAmount(0l);
		Date bestTime = orderInfo.getBestTime();
		int hour = DateUtils.getQuotHours(bestTime, new Date());
		if (hour > 2) {
			order.setOrderType("2");
		} else {
			order.setOrderType("1");
		}
		if (order.getPostscript() == null
			|| "".equals(order.getPostscript().trim())) {
			order.setPostscript("无");
		}
		// 最佳收货时间是明天 最佳配送时间
		List<OrderGoods> orderGoods = orderInfo.getOrderGoods();
		List<HOrderCoupon> orderCoupons = orderInfo.getOrderCoupons();
		Long id = jdbcDao.insert(order);
		orderInfo.setId(id);
		log.info(JSON.toJSONString(orderInfo));
		if (orderGoods != null && orderGoods.size() > 0) {//商品明细处理
			for (HOrderGoods og : orderGoods) {
				HOrderGoods ogs = new HOrderGoods();
				BeanUtils.copyProperties(og, ogs);
				ogs.setOrderId(id);
				ogs.setSellerId(sellerId);
				ogs.setGoodsAttr("0:0:件");
				jdbcDao.insert(ogs);
			}
		}
		if (orderCoupons != null && orderCoupons.size() > 0) {//优惠信息保存
			for (HOrderCoupon oc : orderCoupons) {
				oc.setOrderId(id);
				jdbcDao.insert(oc);
			}
		}
		if (orderInfo.getRedId() != null) {
			ActUser au = new ActUser();
			au.setId(orderInfo.getRedId());
			au.setUserId(order.getUserId());
			au.setOrderId(id);
			au.setStatus("1");//红包使用
			jdbcDao.update(au);
		}
		return orderInfo;
	}

	/**
	 * 商品基础信息sql
	 */
	private static final String goodsDetail = "SELECT a.seller_id supplierId, ( SELECT d. NAME FROM h_seller d WHERE d.id = a.seller_id ) supplierName, a.id id, a.id skuId, a.title goodsName, a.id goodsId, a.cid, a.goods_sn, a.pic_url, c.discount, a.title, a.goods_status, a.goods_detail, '' properties, a.rec_price, a.sale_price, c.`name` actName, c.act_id actId, c.pic_url actPicUrl, c.subtract FROM h_seller_goods a LEFT JOIN v_act_goods c ON a.id = c.goods_id WHERE a.is_delete = '0' AND a.goods_status = '0'";

	/**
	 * 获取商品详细信息
	 * @param skuId
	 * @return
	 */
	@Override
	public SellerGoods getGoodsDetail(Long goodsId) {
		SellerGoods goodsInfo = jdbcDao.queryOneBySQL(goodsDetail + " and a.id = ? ", SellerGoods.class, goodsId);
		return goodsInfo;
	}

	private static final String isFirstOrder = " SELECT (SELECT count(1) FROM h_order c WHERE c.user_id = b.user_id "
		+ " and b.user_id != 0 and c.id != b.id and c.order_status = 'have_done') as is_first FROM h_order b WHERE ";

	/**
	 * 查询某个订单是不是首单
	 */
	public boolean isFirstOrder(Long orderId, String orderSn) {
		String sql = null;
		Integer count = 0;
		if (StringUtils.isBlank(orderSn)) {
			sql = isFirstOrder + "b.id = ?";
			count = jdbcDao.queryOneBySQL(sql, Integer.class, orderId);
		} else {
			sql = isFirstOrder + "b.order_sn = ?";
			count = jdbcDao.queryOneBySQL(sql, Integer.class, orderSn);
		}
		if (count != null && count > 0) {
			return false;
		}
		return true;
	}
}
