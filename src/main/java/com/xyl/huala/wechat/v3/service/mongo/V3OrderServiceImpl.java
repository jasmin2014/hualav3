package com.xyl.huala.wechat.v3.service.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HOrderCoupon;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.entity.TDirt.DirtEnum;
import com.xyl.huala.utils.DateUtils;
import com.xyl.huala.wechat.v3.dao.V3OrderDao;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.domain.CartInfo;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Order;
import com.xyl.huala.wechat.v3.domain.OrderGoods;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.service.V3OrderService;
import com.xyl.huala.wechat.v3.util.BalanceUtils;
import com.xyl.huala.wechat.v3.util.WxSession;

/**
 * 订单处理
 * 
 * @author zxl0047
 *
 */
@Service
@Profile("mongo")
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
		order.setOrderGoods(this.getOrderGoodsList(userId, sellerId));
		order.setGoodAmount(order.caclGoodsAmount());
		order.setOrderSn(orderSn);
		order.setBestTime(new Date());
		Seller seller = orderDao.getSeller(sellerId);
		order.setShippingAmount(seller.getDeliveryAmount());
		order.setDiscountAmount(0L);
		order.setActCart(orderDao.getUserCard(userId, sellerId));
		// order.setGoodAmount(goodsAmount);
		return order;
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
			SellerGoods sg = sellerDao.getGoodsDetail(cart.getSkuId(), cart.getGoodsId());
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
	 * @return
	 */
	public Order preOrderAddress(Order order) {
		HUserAddress a = jdbcDao.get(HUserAddress.class, order.getAddressId());
		if (a == null) {
			return null;
		}
		order.setAddress(a.getAddress());
		order.setAreaId("0");
		order.setCity(a.getCity());
		order.setConsignee(a.getConsignee());
		order.setProvince(a.getProvince());
		order.setDistrict(a.getDistrict());
		order.setSignBuilding(a.getSignBuilding());
		order.setCountry("zh");
		order.setMobile(a.getMobile());
		return order;
	}
	/**
	 * 1.4红包优惠处理
	 * @return
	 */
	public Order preCartCoupons(Order order, Long sellerId) {
		Long redId = order.getRedId();
		if (redId != null && redId != 0l) {
			// 根据红包的id查询红包信息
			ActUser card = orderDao.getRedCardById(redId);
			if (card == null) {
				return order;
			}
			HOrderCoupon coupon = new HOrderCoupon(1, card.getBalance(), 1);
			coupon.setName("红包优惠:"+card.getName());
			coupon.setPlatformPrice(card.getPlatformPrice());
			order.addOrderCoupons(coupon);
		}
		return order;
	}
	/**
	 * 1.5配送优惠处理
	 * @return
	 */
	public Order preShipping(Order order, Long sellerId) {
		if ("1".equals(order.getShippingType())&&order.getShippingAmount()>0) {
			HOrderCoupon coupon = new HOrderCoupon(3, order.getShippingAmount(), 1);
			// 减去使用红包的金额
			coupon.setName("自提免配送费");
			coupon.setPlatformPrice(0l);
			order.addOrderCoupons(coupon);
		}
		return order;
	}
	
	/**
	 * 订单确认：检验订单数据的正确性，然后保存订单数据
	 * 
	 * @param sellerId
	 * @param order
	 *            前台传入的订单数据，后台在取一次，两次数据做检验，如果一致则通过
	 * @return
	 */
	@Transactional
	public DataRet<String> checkOrder(Long sellerId, Order order) {
		DataRet<String> ret = new DataRet<String>();
		Long userId = WxSession.getUserId();
		Long frontOrderAmount=order.getGoodAmount()+order.getShippingAmount()-order.getDiscountAmount();
		List<OrderGoods> goodsList = this.getOrderGoodsList(userId,
				sellerId);
		if (order.getOrderGoods().size() != goodsList.size()) {
			ret.setErrorCode("goods is no eq");
			ret.setMessage("商品信息不一致");
			return ret;
		}
		if(goodsList==null||goodsList.size()<=0){
			ret.setErrorCode("no cart");
			ret.setMessage("没有购物车信息，请选择商品");
			return ret;
		}
		/***************************************************
		 * *************后台订单计算
		 ***************************************************/
		Order confirm = new Order();
		BeanUtils.copyProperties(order, confirm);
		Seller seller=orderDao.getSeller(sellerId);
		confirm.setShippingAmount(seller.getDeliveryAmount());
		confirm.setOrderGoods(goodsList);
		confirm=this.preOrderAddress(confirm);
		confirm=this.preCartCoupons(confirm, sellerId);
		confirm=this.preShipping(confirm, sellerId);
		if(confirm.caclOrderAmount().longValue()!=frontOrderAmount.longValue()){
			ret.setErrorCode("amount is not eq");
			ret.setMessage("商品金额不一致");
		}
		// 下订单失败
		Order o=orderDao.addOrder(confirm, sellerId);
		ret.setBody(o.getId().toString());
		return ret;
	}

	@Override
	public String orderExec(Long orderId, String orderStatus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateOrderFirst(Long orderId, String orderSn) {
		// TODO Auto-generated method stub
	}
}
