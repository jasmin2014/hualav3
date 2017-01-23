/** 
 * 
 */
package com.xyl.huala.wechat.v3.dao.mysql;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerExtends;
import com.xyl.huala.entity.HSsoLog;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.enums.SellerEnum;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.Seller;

/**   
 * @tag
 */
@Repository
@Profile("mysql")
public class V3UserDaoImpl implements V3UserDao{
	
	@Autowired
	private JdbcDao jdbcDao;


	/**
	 * 获取扩展属性值
	 * @param sellerEnum
	 * @return
	 */
	public String getExt(Long sellerId,SellerEnum sellerEnum) {
		HSellerExtends entity = new HSellerExtends(sellerEnum);
		entity.setSellerId(sellerId);
		entity = jdbcDao.queryOne(entity);
		return entity == null ? null : entity.getDataValue();
	}



	/**
	 * 获取地址信息，用户Id与地址ID不能为空
	 * 
	 * @param userId
	 *            用戶id
	 * @param addressId
	 *            地址id
	 * @param isDefault
	 *            是否默认，可以为空,如果是1则是默认
	 * @return
	 */
	public HUserAddress getAddress(Long userId, Long addressId, String isDefault) {
		if(userId==null&&addressId==null){
			return null;
		}
		HUserAddress entity = new HUserAddress(userId, null);
		entity.setId(addressId);
		entity.setIsDefault(isDefault);
		return jdbcDao.queryOne(entity);
	}


	static final String coupon="select a.*,b.name sellerName,IF ( now() BETWEEN  a.start_day AND   a.end_time AND a. STATUS = '0',0,1) AS isInvalid  from act_user a left join h_seller b on a.seller_id=b.id where a.user_id=? order by a.end_time desc;";
 	/**
	 * 得到优惠券信息
	 */
	public List<ActCardExt> getUserCoupon(Long userId) {
		return jdbcDao.queryBySQL(coupon, ActCardExt.class, userId);
	}


	/**
	 * 取得用户收藏店铺列表
	 * @param userId
	 * @return
	 */
	public List<Seller> getCollectsSeller(Long userId) {
		String seller_sql="select a.* from h_seller a, h_user_ext b where a.id=b.data_key and b.group_key='seller_collect' and b.is_valid='1' and b.user_id=?";
		List<Seller> list= jdbcDao.queryBySQL(seller_sql, Seller.class, userId);
		return list;
	}
	
	/**
	 * 查询用户下的订单数
	 * @param district
	 * @return
	 */
	private static final String query_user_order = "select count(1) from h_order a where a.user_id = ? and a.order_status <> 'close' and a.order_status <> 'cancel' ";
	public int getUserOrderCount(Long uid){
		if(uid==null){
			return 0;
		}
		return jdbcDao.queryOneBySQL(query_user_order, Integer.class, uid);
	}
	
	/**
	 * 保存用户操作日志
	 */
	@Async
	public void saveSsoLog(Long userId,Long sellerId,String type,String remark){
		HSsoLog l=new HSsoLog();
		l.setCreateTime(new Date());
		l.setOptType(type);
		l.setRemark(remark);
		l.setSellerId(sellerId);
		l.setUserId(userId);
		jdbcDao.insert(l);
	}

	/**
	 * 根据区号 查询用户的省市编号
	 * @param district
	 * @return
	 */
	private static final String getProvince = " SELECT t3.code province, t2.code city FROM t_region t1 LEFT JOIN t_region t2 ON t1.parent_code = t2.code"
			                                   + " LEFT JOIN t_region t3 ON t2.parent_code = t3.code WHERE t1.code = ? AND t1.type = '2'";
	@Override
	public HUserAddress getProvince(String district) {
		return jdbcDao.queryOneBySQL(getProvince, HUserAddress.class, district);
	}

	/**
	 * 获取用户今日是否领取过每日红包
	 */
	private static final String getRedPackage = "select id,act_cart_id,user_id,name,seller_id,"
			+ "type,sums,balance,platform_price,status from act_user where user_id = ? and type = 3 and add_time "
			+ "between DATE_FORMAT(now(),'%Y-%m-%d 00:00:00') and DATE_FORMAT(now(),'%Y-%m-%d 23:59:59')";
	@Override
	public List<ActUser> getRedPackage(Long userId) {
		return jdbcDao.queryBySQL(getRedPackage, ActUser.class, userId);
	}
}
