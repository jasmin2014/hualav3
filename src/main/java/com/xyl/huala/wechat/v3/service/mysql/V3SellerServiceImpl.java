/**
 *
 */
package com.xyl.huala.wechat.v3.service.mysql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.domain.Gps;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerBanner;
import com.xyl.huala.entity.HSellerManage;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.GoodsActGroup;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;
import com.xyl.huala.wechat.v3.service.V3SellerService;

/**
 * @author leazx
 */
@Service
@Profile("mysql")
public class V3SellerServiceImpl implements V3SellerService {
	@Autowired
	private JdbcDao jdbcDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private MongoTemplate mongo;
	@Autowired
	private V3UserDao userDao;
	@Autowired
	private V3SellerDao sellerDao;

	/**
	 * 获取微信定位的GPS
	 *
	 * @param openid
	 * @return
	 */
	public Gps getLocation(String openid) {
		return (Gps) redisDao.get("wx_gps:" + openid);
	}

	/**
	 * 周边看看数据
	 *
	 * @param gps
	 * @param range
	 * @param page
	 * @return
	 */
	public List<GeoResult<Seller>> getIndexSeller(Point gps, Integer range,
												  PageRequest page) {
		return sellerDao.getIndexSeller(gps, range, page);
	}

	/**
	 * 根据店铺ID取出商家信息
	 *
	 * @param lng
	 * @param lat
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public Seller getSellerById(Long sid) {
		Seller seller = sellerDao.getSellerById(sid);
		return seller;
	}

	/**
	 * 根据经纬度查询最近的商店
	 *
	 * @param lng
	 * @param lat
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public Seller getSellerByGps(Gps gps) {
		if (gps == null) {
			return null;
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("gps")
			.near(new Point(gps.getLng(), gps.getLat())).and("isDelete")
			.is("0").and("sellerStatus").is("0").and("sellerType").is(1));
		Seller seller = mongo.findOne(query, Seller.class);
		if (seller != null) {
			String sql = "select data_value from h_seller_extends where group_key='basic_pro' and data_key = 'seller_status' and seller_id=?";
			String queryOneBySQL = jdbcDao.queryOneBySQL(sql, String.class, seller.getId());
			//是否开店
			seller.setSellerStatus(queryOneBySQL);
		}
		return seller;
	}

	/**
	 * 查询cid下的类目
	 *
	 * @param sid
	 * @param cid
	 * @return
	 */
	// @Cacheable(value=HlCache.GOODS,key="'V2HGoodsService.getCategories('+#sid+','+#cid+')'")
	@Cacheable(value = "goods", key = "'V3SellerServiceImpl.getCategories('+#sid+')'")
	public List<SellerGoodsCat> getCategories(Long sid) {
		Seller seller = this.getSellerById(sid);
		return sellerDao.getCategories(seller, 0L);
	}

	/**
	 * 获取某个店铺的供应商列表
	 *
	 * @param seller
	 * @return
	 */
	public List<Seller> getSupplier(Long sid) {
		Seller seller = this.getSellerById(sid);
		return sellerDao.getSupplier(seller);
	}

	/**
	 * @param sellerId
	 * @param type
	 * @param supplierId
	 * @param page
	 * @return
	 */
	@Cacheable(value = "goods", key = "'V3SellerServiceImpl.getGoodsList('+#sellerId+','+#cateId+','+#supplierId+','+#sort+','+#type+','+#page+','+#size+')'")
	public List<SellerGoods> getGoodsList(Long sellerId, Long cateId,
										  Long supplierId, String sort, String type, Integer page,
										  Integer size) {
		List<SellerGoods> list = sellerDao.getGoodsList(sellerId, cateId, supplierId, sort, type, page, size);
		return list;
	}

	/**
	 * 获取商品详细信息
	 *
	 * @param goodsSkuId 商品SKUID 两个传一个便可
	 * @param goodsId    商品ID
	 * @return
	 */
	public SellerGoods getGoodsDetail(Long goodsSkuId, Long goodsId) {
		SellerGoods sg = sellerDao.getGoodsDetail(goodsSkuId, goodsId);
		return sg;
	}

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取3个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
	@Cacheable(value = "goods", key = "'V3SellerServiceImpl.getGoodsActCat('+#seller.id+')'")
	public List<GoodsActGroup> getGoodsActCat(Seller seller) {
		return sellerDao.getGoodsActCat(seller);
	}

	/**
	 * 获取活动商品列表
	 *
	 * @param seller 店铺信息
	 * @param actId  活动ID
	 * @return
	 */
	public List<SellerGoods> getGoodsActList(Seller seller, Long supplierId,
											 String sort, Long actId, Integer page, Integer size) {
		return sellerDao.getGoodsActList(seller, supplierId, sort, actId, page, size);
	}

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取6个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
	@Cacheable(value = "goods", key = "'V3SellerServiceImpl.getBanner('+#seller.id+')'")
	public List<HSellerBanner> getBanner(Seller seller) {
		return sellerDao.getSellerBannerInfo(seller);
	}

	/**
	 * 店铺首页，是否有红包是否为首单红包，如果是首单则显示，不是首单则不显示
	 *
	 * @param sellerId 店铺ID
	 * @param userId   用户ID
	 * @return
	 */
	public ActCardExt firstRedPackage(Long sellerId, Long userId) {
		// 如果下过单则不显示
		ActCardExt card = sellerDao.queryOneActCard(sellerId, userId);
		return card;
	}

	/**
	 * 店铺首页  红包列表展示
	 * <p>
	 * 包含 {  新手红包  首单红包  活动红包 }
	 *
	 * @param sellerId
	 * @param userId
	 * @return
	 */
	@Override
	public List<ActCardExt> homeRedPackage(Long sellerId, Long userId) {
		List<ActCardExt> actCardExtList = new ArrayList<ActCardExt>();
		//每日红包 根据店铺区域city过滤
		List<ActCardExt> everyDayCardList = sellerDao.everyDayCardList(sellerId, userId);
		//首单红包
		ActCardExt actCardExt = sellerDao.getFirstRedCard(sellerId, userId);
		if (null != actCardExt) {
			actCardExtList.add(actCardExt);
		}
		if (everyDayCardList != null && everyDayCardList.size() > 0) {
			actCardExtList.addAll(everyDayCardList);
		}
		List<ActCardExt> supplierActCardList = new ArrayList<ActCardExt>();
		supplierActCardList= sellerDao.supplierCardList(sellerId, userId);
		if (supplierActCardList!=null && supplierActCardList.size()>0) {
			actCardExtList.addAll(supplierActCardList);
		}
		return actCardExtList;
	}


	/**
	 * 领取红包
	 *
	 * @param sellerId 店铺ID
	 * @param userId   用户ID
	 * @return
	 */
	@Transactional
	public synchronized ActUser getRedPackage(Long sellerId, Long userId,
											  Long actCardId) {

		return sellerDao.getRedPackage(sellerId, userId, actCardId);
	}

	/**
	 * 店铺首页
	 * <p>
	 * 一键领取红包集合
	 *
	 * @param sellerId
	 * @param userId
	 * @param actCartId
	 * @return
	 */
	@Override
	public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCartId) {
		return sellerDao.getRedPackageList(sellerId, userId, actCartId);
	}

	/**
	 * 搜索商品
	 *
	 * @param sellerId
	 * @param key
	 * @param page
	 * @param size
	 * @return
	 */
	public List<SellerGoods> getSearchList(Long sellerId, String key,
										   Integer page, Integer size) {
		List<SellerGoods> list = sellerDao.searchGoods(sellerId, key, page,
			size);
		return list;
	}

	/**
	 * 增加店铺的今日访客数
	 */
	@Override
	public void addSellerVisitorNumber(Long sellerId, Long userId) {
		this.addSellerVisitorNumber(sellerId);
		if (userId != null)
			this.addSellerManage(sellerId, userId);
	}

	/**
	 * 增加店铺的今日访客数
	 */
	@SuppressWarnings("unchecked")
	public void addSellerVisitorNumber(Long sellerId) {
		String hashKey = "sellerId:" + sellerId;
		if (redisDao.hasKey("visitorNumber")) {
			Integer visitorNumber = (Integer) redisDao.get("visitorNumber", hashKey);
			if (visitorNumber == null) visitorNumber = 0;
			visitorNumber = visitorNumber + 1;
			redisDao.put("visitorNumber", hashKey, visitorNumber);
		} else {
			redisDao.put("visitorNumber", hashKey, 1);
		}
	}

	/**
	 * 增加商家的客户关系数据
	 */
	@SuppressWarnings("unchecked")
	public void addSellerManage(Long sellerId, Long userId) {
		HSellerManage manage = new HSellerManage();
		manage.setUserId(userId);
		manage.setSellerId(sellerId);
		manage.setVisitorTime(new Date());
		redisDao.getOperations().opsForList().leftPush("sellerManage", manage);
	}
}
