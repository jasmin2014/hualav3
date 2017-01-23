package com.xyl.huala.wechat.v3.dao.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HSeller;
import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.wechat.v3.dao.V3ScanCodeDao;
import com.xyl.huala.wechat.v3.domain.Seller;
/**
 * 扫码直付的DAO服务实现层
 */
@Repository
@Profile("mysql")
@SuppressWarnings({})
public class V3ScanCodeDaoImpl implements V3ScanCodeDao{
	@Autowired
	private JdbcDao jdbcDao;
	/**
	 * 获取商家信息
	 */
	private static final String getSellerNameById = " select hs.id,hs.name,hs.img_url,hs.is_delete,hse.data_value sellerStatus"
			 				+ " from h_seller hs left join h_seller_extends hse on hs.id = hse.seller_id"
			 				+ " where hs.id = ? and hse.group_key = 'basic_pro' and hse.data_key = 'seller_status'";
	@Override
	public Seller getSellerNameById(Long sid) {
		return jdbcDao.queryOneBySQL(getSellerNameById, Seller.class, sid);
	}
	
	/**
	 * 根据订单id获取订单信息
	 */
	private static final String gerOrderInfoById = "select referer,order_sn,add_time,pay_amount,discount_amount,order_amount from h_order where id = ?";
	@Override
	public HOrder getOrderInfoById(Long id) {
		return jdbcDao.queryOneBySQL(gerOrderInfoById, HOrder.class, id);
	}

	/**
	 * 根据用户ID获取用户信息
	 */
	private static final String getUserInfoById = "select id,mobile,openid from h_sso_user where id = ?";
	@Override
	public HSsoUser getUserInfoByMobile(Long userId) {
		return jdbcDao.queryOneBySQL(getUserInfoById, HSsoUser.class, userId);
	}
	
	/**
	 * 获取商家的地址信息
	 */
	private static final String getSellerAddressById = "select province,city,district,address from h_seller where id = ?";
	@Override
	public HSeller getSellerAddressById(Long sellerId) {
		return jdbcDao.queryOneBySQL(getSellerAddressById, HSeller.class, sellerId);
	}
	
	/**
	 * 获取用户领取但是没有使用的红包信息
	 */
	private static final String getNoUseCard = "select id,type,sums,balance,act_cart_id,platform_price,status from act_user where id = ?"
			  + " and (seller_id = ? or seller_id = 0) and user_id = ? and status = '0' and now() between start_day and end_time";
	@Override
	public ActUser getNoUseCard(Long actUserId, Long sellerId, Long userId) {
		return jdbcDao.queryOneBySQL(getNoUseCard, ActUser.class, actUserId, sellerId, userId);
	}
}
