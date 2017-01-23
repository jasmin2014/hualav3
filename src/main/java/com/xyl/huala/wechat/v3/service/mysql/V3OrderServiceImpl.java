package com.xyl.huala.wechat.v3.service.mysql;

import java.util.*;

import com.xyl.huala.entity.*;
import com.xyl.huala.utils.DateUtils;
import com.xyl.huala.wechat.v3.domain.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.xyl.core.jdbc.persistence.Criteria;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.TDirt.DirtEnum;
import com.xyl.huala.enums.OrderLogEnum;
import com.xyl.huala.wechat.v3.dao.V3BalanceDao;
import com.xyl.huala.wechat.v3.dao.V3OrderDao;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.service.V3OrderService;
import com.xyl.huala.wechat.v3.util.BalanceUtils;
import com.xyl.huala.wechat.v3.util.WxSession;

/**
 * 订单处理
 *
 * @author zxl0047
 */
@Service
@Profile("mysql")
public class V3OrderServiceImpl implements V3OrderService {
	private static Logger log = Logger.getLogger(V3OrderServiceImpl.class);
	@Autowired
	private V3OrderDao orderDao;
	@Autowired
	private V3UserDao userDao;
	@Autowired
	private V3SellerDao sellerDao;
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private V3BalanceDao balanceDao;

	/**
	 * 获取订单信息
	 *
	 * @param orderId
	 * @return
	 */
	public Order getOrderInfo(Long orderId) {
		return orderDao.getOrderInfo(orderId);
	}

	/**
	 * 获取订单详情
	 *
	 * @param userId
	 * @param b
	 * @return
	 */
	public List<Order> getOrderList(Long userId, boolean b) {
		return orderDao.getOrderList(userId, b);
	}

	/**
	 * 确认下单，根据购物车获取店铺的商品信息
	 *
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	public Order getOrderConfirm(Long userId, Long sellerId) {
		String property = System.getProperty(DirtEnum.订单号前缀.getDirtKey());
		String orderSn = property + new Date().getTime();
		Order order = new Order();
		List<OrderGoods> orderGoodsList = this.getOrderGoodsList(userId, sellerId);
		order.setOrderGoods(orderGoodsList);
		order.setGoodAmount(order.caclGoodsAmount());
		order.setOrderSn(orderSn);
		Date now = DateUtils.getDateFromStr(DateUtils.getDateTime(new Date(), "yyyy-MM-dd HH:00:00"));
		order.setBestTime(DateUtils.addMinutes(now, 120));
		Seller seller = orderDao.getSeller(sellerId);
		if (seller.getFreeAmount() <= order.getGoodAmount()) {//如果商品金额大于免配送金额则配送费为0
			order.setShippingAmount(0L);
		} else {
			order.setShippingAmount(seller.getDeliveryAmount());
		}
		order.setDiscountAmount(0L);
		List<ActCardExt> actCardExtList = orderDao.getUserCard(userId, sellerId);
		if (actCardExtList != null && actCardExtList.size() > 0) {
			actCardExtList = checkCouponReturnActCardExtList(actCardExtList, orderGoodsList, order, sellerId);
		}
		order.setActCart(actCardExtList);
		return order;
	}

	/**
	 * 检查优商品金额是否满足优惠券满减条件,并且返回优惠券集合
	 *
	 * @param actCardExtList 优惠券集合
	 * @param orderGoodsList 商品集合
	 * @return
	 */
	public List<ActCardExt> checkCouponReturnActCardExtList(
		List<ActCardExt> actCardExtList,
		List<OrderGoods> orderGoodsList, Order order, Long sellerId1) {

		//供应商使用红包type==4  供应商(sellerId !=supplierId)   合计供应商总金额  红包是否满足金额使用条件
		//非供应商type!=4 商品金额是否满足  红包使用金额条件
		//供应商总金额
		order.setSellerId(order.getSellerId() == null ? sellerId1 : order.getSellerId());
		long supplierTotal = 0;
		//定义优惠券集合
		List<ActCardExt> actCardExtList1 = new ArrayList<ActCardExt>();

		for (OrderGoods orderGoods : orderGoodsList) {
			//供应商计算总金额
			if (!orderGoods.getSellerId().equals(orderGoods.getSupplierId())) {
				supplierTotal += orderGoods.getSalePrice() * orderGoods.getGoodsNumber();
			}
		}

		for (ActCardExt actCardExt : actCardExtList) {
			Long sellerId = actCardExt.getSellerId() == null ? 0l : actCardExt.getSellerId();
			//供应商红包判断总金额
			if (actCardExt.getType() == 4) {
				if(supplierTotal >= actCardExt.getSums() && sellerId.equals(new Long(0))){
					actCardExtList1.add(actCardExt);
				}
				continue;
				//每日红包判断订单金额
			}
			if (sellerId.equals(order.getSellerId()) || sellerId.equals(new Long(0))) {
				if (order.getGoodAmount() >= actCardExt.getSums()) {
					actCardExtList1.add(actCardExt);
				}
			}
		}
		return actCardExtList1;
	}

	/**
	 * 根据购物车信息获取商品列表
	 *
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	private List<OrderGoods> getOrderGoodsList(Long userId, Long sellerId) {
		List<CartInfo> cartList = orderDao.getCartList(userId, sellerId);
		Long goodsAmount = 0L;
		List<OrderGoods> goodsList = new ArrayList<>();
		for (CartInfo cart : cartList) {
			OrderGoods og = new OrderGoods();
			SellerGoods sg = orderDao.getGoodsDetail(cart.getSkuId());//此处SKUID其实是goodsId
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
			og.setGoodsNumber(cart.getGoodNum());
			og.setRecPrice(sg.getRecPrice());
			goodsAmount += og.getSalePrice() * og.getGoodsNumber();
			goodsList.add(og);
		}
		return goodsList;
	}

	/**
	 * 计算开店时间列表，用来供前台选择最佳收货时间
	 *
	 * @param sellerId
	 * @return
	 */
	public Map<String, JSONObject> chooseDate(Long sellerId) {
		Map<String, JSONObject> m = new LinkedHashMap<>();
		Seller seller = orderDao.getSeller(sellerId);
		BalanceUtils b = new BalanceUtils();
		List aaa = b.getTodayList(DateUtils.getHoursMinute(seller.getOpenTime()), DateUtils.getHoursMinute(seller.getEndTime()));
		JSONObject j = new JSONObject();
		j.put("list", aaa);
		j.put("name", "今天");
		m.put("today", j);
		aaa = b.getTomorrowList(DateUtils.getHoursMinute(seller.getOpenTime()), DateUtils.getHoursMinute(seller.getEndTime()));
		j = new JSONObject();
		j.put("list", aaa);
		j.put("name", "明天");
		m.put("toworrow", j);
		return m;
	}

	/**
	 * 1.1下单地址预处理
	 *
	 * @param order
	 * @param seller
	 * @return
	 */
	private Order preOrderAddress(Order order, Seller seller) {
		HUserAddress a = jdbcDao.get(HUserAddress.class, order.getAddressId());
		if (a == null) {
			if ("1".equals(order.getShippingType())) {//如果是自提，地址可以为空，为空时候，则取微信信息
				HSsoUser h = jdbcDao.get(HSsoUser.class, order.getUserId());
				order.setAddress(seller.getAddress());
				order.setAreaId("0");
				order.setCity(seller.getCity());
				order.setProvince(seller.getProvince());
				order.setDistrict(seller.getDistrict());
				order.setConsignee(h.getUserName());
				order.setSignBuilding(seller.getAddress());
				order.setCountry("zh");
				order.setMobile(h.getMobile());
			} else {
				return null;
			}
		} else {
			order.setAddress(a.getAddress());
			order.setAreaId("0");
			order.setCity(a.getCity());
			order.setConsignee(a.getConsignee());
			order.setProvince(a.getProvince());
			order.setDistrict(a.getDistrict());
			order.setSignBuilding(a.getSignBuilding());
			order.setCountry("zh");
			order.setMobile(a.getMobile());
		}
		return order;
	}

	/**
	 * 1.4红包优惠处理
	 *
	 * @param order
	 * @param sellerId
	 * @return
	 */
	private Order preCartCoupons(Order order, Long sellerId) {

		Long redId = order.getRedId();
		if (redId != null && redId != 0l) {
			// 根据红包的id查询红包信息
			ActUser card = orderDao.getRedCardById(redId);
			if (card == null) {
				return order;
			}
			HOrderCoupon coupon = new HOrderCoupon(1, card.getBalance(), 1);
			coupon.setName("红包优惠:" + card.getName());
			coupon.setPlatformPrice(card.getPlatformPrice());
			//记录订单优惠   act_user表 id 字段   act_user表  seller字段
			coupon.setSupplierId(card.getSellerId());
			coupon.setActUserId(card.getId());

			order.addOrderCoupons(coupon);
		}
		return order;
	}

	/**
	 * 1.5配送优惠处理
	 *
	 * @param order
	 * @param seller
	 * @return
	 */
	private Order preShipping(Order order, Seller seller) {
		if ("1".equals(order.getShippingType()) && order.getShippingAmount() > 0) {
			HOrderCoupon coupon = new HOrderCoupon(3, order.getShippingAmount(), 1);
			// 减去使用红包的金额
			coupon.setName("自提免配送费");
			coupon.setPlatformPrice(0l);
			order.addOrderCoupons(coupon);
		} else {
			// 查询有没有免配送
			Long avoidAmount = seller.getFreeAmount();
			if (avoidAmount != null) {
				// 免配送费
				if (order.getGoodAmount() >= avoidAmount) {
					HOrderCoupon coupon = new HOrderCoupon(3, order.getShippingAmount(), 1);
					// 减去使用红包的金额
					coupon.setName("满" + avoidAmount + "免配送费");
					coupon.setPlatformPrice(0l);
					order.addOrderCoupons(coupon);
				}
			}
		}
		return order;
	}

	/**
	 * 订单确认：检验订单数据的正确性，然后保存订单数据
	 *
	 * @param sellerId
	 * @param order    前台传入的订单数据，后台在取一次，两次数据做检验，如果一致则通过
	 * @return
	 */
	@Transactional
	public DataRet<String> checkOrder(Long sellerId, Order order) {
		DataRet<String> ret = new DataRet<String>();
		Long userId = WxSession.getUserId();
		if (!"1".equals(order.getShippingType()) && order.getAddressId() == null) {
			ret.setErrorCode("address is null");
			ret.setMessage("配送地址不能为空");
			return ret;
		}
		Long frontOrderAmount = order.getGoodAmount() + order.getShippingAmount() - order.getDiscountAmount();
		List<OrderGoods> goodsList = this.getOrderGoodsList(userId,
			sellerId);
		if (order.getOrderGoods().size() != goodsList.size()) {
			ret.setErrorCode("goods is no eq");
			ret.setMessage("商品信息不一致");
			return ret;
		}
		if (goodsList == null || goodsList.size() <= 0) {
			ret.setErrorCode("no cart");
			ret.setMessage("没有购物车信息，请选择商品");
			return ret;
		}
		Seller seller = orderDao.getSeller(sellerId);
		if ("1".equals(seller.getSellerStatus())) {
			ret.setErrorCode("seller is close");
			ret.setMessage("该店铺暂停营，请选择其他店铺购物！！！");
			return ret;
		}
		/***************************************************
		 * *************后台订单计算
		 ***************************************************/
		Order confirm = new Order();
		BeanUtils.copyProperties(order, confirm);
		confirm.setShippingAmount(seller.getDeliveryAmount());
		confirm.setOrderGoods(goodsList);
		confirm.setGoodAmount(confirm.caclGoodsAmount());
		confirm = this.preOrderAddress(confirm, seller);
		confirm = this.preCartCoupons(confirm, sellerId);
		confirm = this.preShipping(confirm, seller);
		if (confirm.caclOrderAmount().longValue() != frontOrderAmount.longValue()) {
			ret.setErrorCode("amount is not eq");
			ret.setMessage("商品金额有变更，请重新查看购物车");
			return ret;
		}
		// 下订单失败
		Order o = orderDao.addOrder(confirm, sellerId);
		ret.setBody(o.getId().toString());
		return ret;
	}

	@Override
	@Transactional
	public String orderExec(Long orderId, String orderStatus) {
		if("have_done".equals(orderStatus)) {
			// 判断是不是首单
			this.updateOrderFirst(orderId, null);
		}
		HOrder oldOrder = jdbcDao.get(HOrder.class, orderId);
		if (!OrderLogEnum.NO_PAY.getOptKey().equals(oldOrder.getOrderStatus()) && OrderLogEnum.CANCEL.getOptKey().equals(orderStatus)) {
			return "状态不是末付款，无法取消";
		}
		com.xyl.core.jdbc.persistence.Criteria create = com.xyl.core.jdbc.persistence.Criteria.create(HOrder.class);
		create.set("order_status", orderStatus).set("shipping_time", new Date()).where("id", orderId);
		jdbcDao.update(create);
		if (OrderLogEnum.HAVE_DONE.getOptKey().equals(orderStatus)) {
			create = com.xyl.core.jdbc.persistence.Criteria.create(HSellerOrder.class);
			create.set("order_status", orderStatus)
			      .set("shipping_time", new Date())
			      .where("order_id", orderId);
			jdbcDao.update(create);
			//balanceDao.balanceExec(orderId);
		}
		if (OrderLogEnum.CANCEL.getOptKey().equals(orderStatus)) {
			// 判断用户是否有使用优惠券或者是红包
			Criteria criteria = new Criteria(ActUser.class);
			criteria.set("order_id", null).set("status", "0");
			criteria.where("order_id", orderId);
			jdbcDao.update(criteria);
		}
		return null;
	}
	
	/**
	 * 更新为首单
	 * @param orderId
	 * @param orderSn
	 */
	public void updateOrderFirst(Long orderId, String orderSn) {
		if(orderDao.isFirstOrder(orderId, orderSn)) {
			if(StringUtils.isBlank(orderSn)) { 
				// 更新订单未首单
				jdbcDao.update(new Criteria(HOrder.class).set("is_first", "1").where("id", orderId));
				jdbcDao.update(new Criteria(HSellerOrder.class).set("is_first", "1").where("order_id", orderId));
			} else {
				// 更新订单未首单
				jdbcDao.update(new Criteria(HOrder.class).set("is_first", "1").where("order_sn", orderSn));
				jdbcDao.update(new Criteria(HSellerOrder.class).set("is_first", "1").where("order_sn", orderSn));
			}
		}
	}
}
