package com.xyl.huala.wechat.v3.service.mysql;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.xyl.huala.utils.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xyl.core.jdbc.persistence.Criteria;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HOrderCoupon;
import com.xyl.huala.entity.HOrderGoods;
import com.xyl.huala.entity.HSeller;
import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.entity.TDirt.DirtEnum;
import com.xyl.huala.enums.OrderLogEnum;
import com.xyl.huala.wechat.v3.dao.V3OrderDao;
import com.xyl.huala.wechat.v3.dao.V3ScanCodeDao;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.V3ScanCodeService;
import com.xyl.huala.wechat.v3.util.BalanceUtils;
import com.xyl.huala.weixin.util.Pic;

/**
 * 扫码直付的服务层接口实现
 */
@Service
@Profile("mysql")
public class V3ScanCodeServiceImpl implements V3ScanCodeService {
	@Autowired
	private V3ScanCodeDao scanCodeDao;
	@Autowired
	private V3SellerDao sellerDao;
	@Autowired
	private V3OrderDao orderDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private JdbcDao jdbcDao;

	/**
	 * 根据店铺ID取出商家和活动信息
	 */
	@Override
	public Seller getSellerActInfoById(Long sid) {
		// 获取商家的店铺名称
		Seller seller = scanCodeDao.getSellerNameById(sid);
		if (seller != null && !StringUtils.isBlank(seller.getImgUrl())) {
			seller.setImgUrl(System.getProperty("server_cfg.imgServer") + seller.getImgUrl());
		}
		return seller;
	}

	/**
	 * 扫码直付 新增订单信息
	 * payAmount:用户的支付金额
	 * amount:用户输入的金额
	 */
	@Transactional
	public DataRet<String> addOrderInfo(Long sellerId, String payAmount, String amount,
										String actCardId, String actUserId, Long userId, String isWxScan, String openId, String aliPayId) {
		DataRet<String> ret = new DataRet<String>();
        // 判断是否同一个用户重复提交
        if (this.isRepeat(sellerId, userId)) {
            ret.setErrorCode("pay_error");
            ret.setMessage("您操作太频繁,请过5秒钟后再操作！");
            return ret;
        }
		Long a = NumberUtils.toFen(amount);
		if(a <= 0) {
			ret.setErrorCode("pay_error");
			ret.setMessage("支付的金额不能小于等于0元！");
			return ret;
		}
		if(a > 500000) {
			ret.setErrorCode("pay_error");
			ret.setMessage("支付的金额不能大于5000元！");
			return ret;
		}
		HOrder order = this.preOrderInfo(amount, sellerId, isWxScan);
		HOrderCoupon coupon = null;
		ActUser card = null;
		if (userId == null) {
			if (StringUtils.isNotBlank(openId)) {
				order.setOpenid(openId);
			} else {
				order.setOpenid(aliPayId);
			}
			// 说明用户没有登录 没有优惠金额
			order.setMobile("无");
			order.setUserId(0l);
			order.setConsignee("无");
		} else {
			// 用户登录 查询用户的注册信息 和商家的活动信息
			HSsoUser user = scanCodeDao.getUserInfoByMobile(userId);
			if (!StringUtils.isBlank(actCardId) && !StringUtils.equals("0", actCardId)) {
				// 用户没有领取红包
				if (actCardId != null && actUserId == null) {
					// 用户领取红包
					card = sellerDao.getRedPackage(sellerId, userId, Long.parseLong(actCardId));
					if (card == null) {
						ret.setErrorCode("red_package_is_empty");
						ret.setMessage("客官您来晚了！本店今日红包奖励已经用完了！");
						return ret;
					}
				} else {
					// 查询用户领取并且没有使用的红包信息
					card = scanCodeDao.getNoUseCard(Long.parseLong(actUserId), sellerId, userId);
				}
				coupon = this.preOrderAmpuntByActRule(order, card);
			}
			order.setMobile(user.getMobile());
			order.setUserId(user.getId());
			order.setConsignee(user.getMobile());
			order.setOpenid(user.getOpenid());
		}
		// 插入订单信息
		Long orderId = jdbcDao.insert(order);
		order.setId(orderId);
		// 插入订单商品信息
		this.addHOrderGoods(order);
		ret.setBody(String.valueOf(orderId));
		// 如果优惠信息不为空 则保存优惠信息
		if (coupon != null) {
			coupon.setOrderId(orderId);
			jdbcDao.insert(coupon);
			// 红包使用成功 更新为已使用
			Criteria criteria = new Criteria(ActUser.class);
			jdbcDao.update(criteria.set("order_id", orderId)
				.set("use_time", new Date()).set("status", "1")
				.where("id", card.getId()));
		}
		return ret;
	}

	/**
	 * 判断是否同一个用户重复提交
	 */
	@SuppressWarnings("unchecked")
	private synchronized boolean isRepeat(Long sellerId, Long userId) {
		String key = "scan_pay:" + sellerId + ":" + userId;
		if (redisDao.hasKey(key)) {
			return true;
		}
		redisDao.set(key, key, 5, TimeUnit.SECONDS);
		return false;
	}

	/**
	 * 查询商家的活动 计算订单金额  并且保存订单的优惠信息
	 */
	public HOrderCoupon preOrderAmpuntByActRule(HOrder order,
												ActUser card) {
		HOrderCoupon coupon = null;
		// 有满减券信息 并且卡券没有失效
		if (card != null && card.getSums() != null) {
			// 支付金额 大于等于满减券的使用条件  则支付金额减去相应的优惠金额
			if (order.getGoodAmount() >= card.getSums()) {
				// 红包领取成功之后 使用优惠信息
				order.setDiscountAmount(card.getBalance() == null ? 0l : card.getBalance());
				order.setOrderAmount(order.getGoodAmount() - card.getBalance());
				// 保存优惠信息
				coupon = new HOrderCoupon(1, card.getBalance(), 1);
				coupon.setName(card.getName() == null ? "扫码直付-卡券优惠:" : card.getName());
				coupon.setPlatformPrice(card.getPlatformPrice());
			}
		}
		return coupon;
	}

	/**
	 * 扫码直付组装订单信息
	 *
	 * @param amount
	 * @param sid
	 */
	public HOrder preOrderInfo(String amount, Long sid, String isWxScan) {
		// 组装订单的基本信息
		HOrder order = new HOrder();
		String orderSn = System.getProperty(DirtEnum.订单号前缀.getMetaKey() + "."
			+ DirtEnum.订单号前缀.getDataKey()) + new Date().getTime();
		order.setOrderSn(orderSn);
		order.setAreaId("0");
		order.setSellerId(sid);
		order.setOrderStatus(OrderLogEnum.NO_PAY.getOptKey());
		order.setConsignee("");
		order.setReferer(String.valueOf(sid));
		order.setSignBuilding("");
		order.setAddress("无");
		order.setCountry("zh");
		order.setUserId(0l);
		HSeller address = scanCodeDao.getSellerAddressById(sid);
		order.setSignBuilding(address.getAddress());
		order.setAddress(address.getAddress());
		// 查询用户的地址信息
		order.setProvince(address.getProvince());
		order.setCity(address.getCity());
		order.setDistrict(address.getDistrict());
		order.setOrderType("5");
		order.setPostscript("无");
		order.setShippingType("1");
		Long a = NumberUtils.toFen(amount);
		order.setGoodAmount(a);
		order.setAddTime(new Date());
		order.setSurplusAmount(0l);
		order.setBestTime(new Date());
		order.setShippingAmount(0l);
		order.setDiscountAmount(0l);
		order.setOrderAmount(a);
		return order;
	}

	/**
	 * 扫码直付 组装订单商品信息
	 */
	public void addHOrderGoods(HOrder order) {
		HOrderGoods goods = new HOrderGoods();
		goods.setOrderId(order.getId());
		goods.setGoodsSn("6923450682102");
		goods.setSellerGoodsId(0l);
		goods.setGoodsName("扫码直付商品");
		goods.setGoodsNumber(1);
		goods.setGoodsAttr("0:0:件");
		goods.setPurchaserStatus("have_purchase");
		goods.setSupplierId(order.getSellerId());
		goods.setSellerSkuId(0l);
		goods.setSellerId(order.getSellerId());
		goods.setSalePrice(order.getGoodAmount());
		goods.setRecPrice(order.getGoodAmount());
		// 保存订单信息
		jdbcDao.save(goods);
	}

	/**
	 * 根据订单id获取订单信息
	 */
	@Override
	public HOrder getOrderInfoById(Long id) {
		return scanCodeDao.getOrderInfoById(id);
	}

	/**
	 * 生成支付二维码
	 *
	 * @param sellerId 店铺id
	 * @param content  二维码内容
	 * @param logoUrl  logo路径
	 */
	@Override
	public BufferedImage genQrcode(Long sellerId, String content, String logoUrl) {
		// 生成二维码图片
		BufferedImage qrcode = Pic.createImage(content);
		try {
			// 向二维码图片中插入logo
			BufferedImage createPicTwo2 = Pic.createPicTwo2(qrcode, logoUrl);
			// 查询店铺的名称
			Seller seller = scanCodeDao.getSellerNameById(sellerId);
			String sellerName = "花啦生活";
			if (seller != null && !StringUtils.isBlank(seller.getName()))
				sellerName = seller.getName();
			return Pic.addWord(createPicTwo2, sellerName);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取商家名称
	 */
	@Override
	public String getSellerNameById(Long sellerId) {
		Seller seller = scanCodeDao.getSellerNameById(sellerId);
		return seller == null ? "花啦生活" : seller.getName();
	}

	/**
	 * 查询用户的红包信息
	 */
	@Override
	public ActCardExt getRedPackage(Long sellerId, Long userId) {
		// 查询用户的红包信息
		ActCardExt card = sellerDao.queryOneActCard(sellerId, userId);
		if (card == null) {
			// 查询用户是否已经领取 但是没有使用的红包
			List<ActCardExt> cards = orderDao.getUserCard(userId, sellerId);
			if (cards != null && cards.size() > 0) {
				for (ActCardExt c : cards) {
					if (4 != c.getType()) {
						return c;
					}
				}
			}
		} else {
			// 给actCartId字段赋值 前端判断使用
			card.setActCartId(card.getId());
			card.setId(null);
		}
		return card;
	}
}
