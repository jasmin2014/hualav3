/**
 *
 */
package com.xyl.huala.wechat.v3.service.mongo;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xyl.huala.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.domain.Gps;
import com.xyl.huala.entity.ActCard;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerBanner;
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
@Profile("mongo")
public class V3SellerServiceImpl implements V3SellerService {

	@Autowired
	private JdbcDao jdbcDao;
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
		Query query = Query.query(Criteria.where("isDelete").is("0")
			.and("sellerStatus").is("0"));
		NearQuery near = NearQuery.near(gps)
			.maxDistance(range, Metrics.KILOMETERS).query(query);
		near.with(page);
		GeoResults<Seller> list = mongo.geoNear(near, Seller.class);
		for (GeoResult<Seller> s : list.getContent()) {
			Seller seller = s.getContent();
			// 取商品列表
			List<SellerGoods> goodsList = mongo.find(
				Query.query(Criteria.where("sellerId").is(seller.getId()))
					.limit(3), SellerGoods.class);
			seller.setGoodsList(goodsList);
		}
		return list.getContent();
	}

	/**
	 * 根据店铺ID取出商家信息
	 * @return
	 */
	public Seller getSellerById(Long sid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(sid).and("isDelete").is("0"));
		Seller seller = mongo.findOne(query, Seller.class);
		return seller;

	}

	/**
	 * 根据经纬度查询最近的商店
	 * @return
	 */
	public Seller getSellerByGps(Gps gps) {
		Query query = new Query();
		query.addCriteria(Criteria.where("gps")
			.near(new Point(gps.getLng(), gps.getLat())).and("isDelete")
			.is("0").and("sellerStatus").is("0"));
		Seller seller = mongo.findOne(query, Seller.class);
		return seller;
	}

	/**
	 * 查询cid下的类目
	 * @param sid
	 * @return
	 */
	// @Cacheable(value=HlCache.GOODS,key="'V2HGoodsService.getCategories('+#sid+','+#cid+')'")
	public List<SellerGoodsCat> getCategories(Long sid) {
		Seller seller = this.getSellerById(sid);
		return sellerDao.getCategories(seller, 0L);
	}

	/**
	 * 获取某个店铺的供应商列表
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
	public List<SellerGoods> getGoodsList(Long sellerId, Long cateId,
										  Long supplierId, String sort, String type, Integer page,
										  Integer size) {
		Query query = new Query();
		Seller seller = this.getSellerById(sellerId);
		Criteria crit = Criteria.where("sellerId");
		if (supplierId != null) {
			crit.is(supplierId);
		} else {
			crit.in(seller.getSupplierList());
		}
		if (cateId != null && cateId != 0) {
			if ("1".equals(type)) {
				List<SellerGoodsCat> cate = sellerDao.getCategories(seller,
					cateId);
				Set<Long> s = new HashSet<>();
				for (SellerGoodsCat c : cate) {
					s.add(c.getCid());
				}
				crit.and("cid").in(s);
			} else {
				crit.and("cid").is(cateId);
			}
		}
		query.addCriteria(crit);
		query.with(new PageRequest(page - 1, size > 100 ? 100 : size));
		if (sort != null) {
			query.with(new Sort(Sort.Direction.ASC, sort));
		}
		List<SellerGoods> list = mongo.find(query, SellerGoods.class);
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
		return sellerDao.getGoodsDetail(goodsSkuId, goodsId);
	}

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取3个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
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
	public List<SellerGoods> getGoodsActList(Seller seller, Long supplierId, String sort, Long actId, Integer page, Integer size) {
		return sellerDao.getGoodsActList(seller, supplierId, sort, actId, page, size);
	}


	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取6个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
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
		int count1 = userDao.getUserOrderCount(userId);
		if (count1 > 0) {
			return null;
		}
		return sellerDao.queryOneActCard(sellerId, userId);
	}

	@Override
	public List<ActCardExt> homeRedPackage(Long sellerId, Long userId) {
		return null;
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
		ActCard actCard = jdbcDao.get(ActCard.class, actCardId);
		int limit = actCard.getCardSurplus();
		if (limit <= 0) {
			return null;
		}
		actCard.setCardSurplus(--limit);
		jdbcDao.update(actCard);
		ActUser a = new ActUser();
		a.setActCartId(actCard.getId());
		a.setSums(actCard.getSums());
		a.setBalance(actCard.getBalance());
		a.setStartDay(actCard.getStartDay());
		Date endd = DateUtils.addDays(new Date(), actCard.getValidDays());
		if (endd.before(actCard.getEndTime())) {
			a.setEndTime(endd);
		} else {
			a.setEndTime(actCard.getEndTime());
		}
		a.setAddTime(new Date());
		a.setUserId(userId);
		a.setType(1);
		jdbcDao.insert(a);
		return a;
	}

	@Override
	public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCartId) {
		return null;
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
		return sellerDao.searchGoods(sellerId, key, page, size);
	}

	/**
	 * 根据店铺ID取出商家和活动信息
	 */
	public Seller getSellerActInfoById(Long sid) {
		return null;
	}

	@Override
	public void addSellerVisitorNumber(Long sellerId, Long userId) {

	}
}
