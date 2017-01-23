/** 
 * 
 */
package com.xyl.huala.wechat.v3.dao.mongo;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
@Profile("mongo")
public class V3UserDaoImpl implements V3UserDao{
	
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private MongoTemplate mongo;
	

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



	/**
	 * 得到优惠券信息
	 */
	public List<ActCardExt> getUserCoupon(Long userId) {
		return jdbcDao.queryByCode("coupon.getUserCoupon", ActCardExt.class, userId);
	}


	/**
	 * 取得用户收藏店铺列表
	 * @param userId
	 * @return
	 */
	public List<Seller> getCollectsSeller(Long userId) {
		String seller_sql=" select data_value from h_user_ext c where c.group_key='seller_collect' and c.user_id=? and is_valid='1'";
		List<Long> list= jdbcDao.queryBySQL(seller_sql, Long.class, userId);
		List<Seller> seller = mongo.find(Query.query(Criteria.where("_id").in(list)), Seller.class);
		return seller;
	}
	
	/**
	 * 查询用户下的订单数
	 * @param district
	 * @return
	 */
	private static final String query_user_order = "select count(1) from h_order a where a.user_id = ? and a.order_status <> 'close' and a.order_status <> 'cancel' and a.order_status <> 'no_pay'";
	public int getUserOrderCount(Long uid){
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



	@Override
	public HUserAddress getProvince(String district) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActUser> getRedPackage(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}
}
