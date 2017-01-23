package com.xyl.huala.wechat.v3.dao.mongo;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerBanner;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.GoodsActGroup;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;

/**
 * @tag
 */
@Repository
@Profile("mongo")
@SuppressWarnings({})
public class V3SellerDaoImpl implements V3SellerDao {

	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private MongoTemplate mongo;

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
			.and("sellerStatus").is("0").and("sellerType").is(1));
		NearQuery near = NearQuery.near(gps)
			.maxDistance(range, Metrics.KILOMETERS).query(query);
		near.with(page);
		GeoResults<Seller> list = mongo.geoNear(near, Seller.class);
		String goodsListSql = "select * from h_seller_goods where seller_id=?";
		for (GeoResult<Seller> s : list.getContent()) {
			Seller seller = s.getContent();
			// 取商品列表
			List<SellerGoods> goodsList = jdbcDao.queryBySQL(goodsListSql, SellerGoods.class, seller.getId());
			seller.setGoodsList(goodsList);
		}
		return list.getContent();
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
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(sid));
		Seller seller = mongo.findOne(query, Seller.class);
		return seller;

	}

	/**
	 * 查询cid下的类目
	 *
	 * @param sid
	 * @param cid
	 * @return
	 */
	// @Cacheable(value="goods",key="'V3SellerDao.getCategories('+#seller.id+','+#cid+')'")
	public List<SellerGoodsCat> getCategories(Seller seller, Long cid) {
		Criteria crit = Criteria.where("sellerId").in(seller.getSupplierList())
			.and("parentCid").is(cid);
		Aggregation agg = newAggregation(match(crit), group("cid", "cname")
			.max("catImg").as("catImg"));
		AggregationResults<SellerGoodsCat> sg = mongo.aggregate(agg,
			SellerGoodsCat.class, SellerGoodsCat.class);
		List<SellerGoodsCat> list = sg.getMappedResults();
		for (SellerGoodsCat gc : list) {
			gc.setSubCatList(this.getCategories(seller, gc.getCid()));
		}
		return list;
	}

	/**
	 * 获取某个店铺的供应商列表
	 *
	 * @param seller
	 * @return
	 */
	public List<Seller> getSupplier(Seller seller) {
		Criteria in = Criteria.where("id").in(seller.getSupplierList());
		Query query = Query.query(in);
		List<Seller> list = mongo.find(query, Seller.class);
		return list;
	}

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取3个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
	public List<GoodsActGroup> getGoodsActCat(Seller seller) {
		Criteria crit = Criteria.where("sellerId").in(seller.getSupplierList())
			.and("act").exists(true);
		Aggregation agg = newAggregation(match(crit),
			group("act.actId", "act.name", "act.content"));
		AggregationResults<GoodsActGroup> sg = mongo.aggregate(agg,
			SellerGoods.class, GoodsActGroup.class);
		List<GoodsActGroup> list = sg.getMappedResults();
		for (GoodsActGroup g : list) {
			crit = Criteria.where("sellerId").in(seller.getSupplierList())
				.and("act.actId").is(g.getActId()).and("act.discount")
				.exists(true);
			Query q = Query.query(crit).with(new PageRequest(0, 3));
			g.setGoodsList(mongo.find(q, SellerGoods.class));
		}
		return list;
	}

	/**
	 * 获取活动商品列表
	 *
	 * @param seller 店铺信息
	 * @param actId  活动ID
	 * @return
	 */
	@Override
	public List<SellerGoods> getGoodsActList(Seller seller, Long supplierId, String sort, Long actId, Integer page, Integer size) {
		Criteria crit = Criteria.where("act.discount").exists(true).and("act.actId").is(actId);
		if (supplierId == null) {
			crit.and("sellerId").in(seller.getSupplierList());
		} else {
			crit.and("sellerId").is(supplierId);
		}
		Query q = Query.query(crit).with(new PageRequest(page - 1, size > 100 ? 100 : size));
		if (sort != null) {
			q.with(new Sort(Sort.Direction.ASC, sort));
		}
		return mongo.find(q, SellerGoods.class);
	}

	@Override
	public List<HSellerBanner> getSellerBannerInfo(Seller seller) {
		return null;
	}

	/**
	 * 查找店铺的banner图
	 *
	 * @param sellerId
	 * @param bannerType user 用户端显示 or seller 商户端显示
	 * @return
	 */
	public List<HSellerBanner> getSellerBannerInfo(Long sellerId) {
		StringBuffer sql = new StringBuffer();
		sql.append(
			"SELECT a.id,a.banner_name,a.url,a.sort,a.img_url,a.start_time,a.end_time ")
			.append("FROM h_seller_banner a INNER JOIN h_seller_extends b ON a.id = b.data_key ")
			.append("AND b.group_key = 'seller_banner' AND b.seller_id = ? ")
			.append("WHERE a.is_delete = 0 AND a.banner_type = 'user' and now() BETWEEN a.start_time and a.end_time ")
			.append("ORDER BY sort DESC ");
		return jdbcDao
			.queryBySQL(sql.toString(), HSellerBanner.class, sellerId);
	}

	/**
	 * 获取店铺中没有失效的红包信息
	 */
	private static final String find_redpacket = "select a.*,c.`name` sellerName from act_card a INNER JOIN h_seller c on a.seller_id = c.id  LEFT JOIN act_info b on a.act_id = b.id and b.`status` = 0 "
		+ "where a.seller_id = ? and a.issue = '1' and a.type = 1 and a.is_delete = '0' and (a.card_num_limit = -1 or (a.card_num_limit >0 and card_surplus>0 ))  and NOW() BETWEEN a.start_day and a.end_time";
	/**
	 * 当前红包是否已领取过
	 */
	private static final String find_user_redpacket = "select count(1) from act_user a  where a.user_id =  ? and a.act_cart_id = ? and (a.`status` = '0' OR a.`status` = '1')";

	public ActCardExt queryOneActCard(Long sellerId, Long userId) {
		ActCardExt actCard = jdbcDao.queryOneBySQL(find_redpacket,
			ActCardExt.class, sellerId);
		if (actCard != null) {
			int userCartCount = jdbcDao.queryOneBySQL(find_user_redpacket,
				Integer.class, userId, actCard.getId());
			if (userCartCount <= 0) {
				return actCard;
			}
		}
		return null;
	}

	public List<ActCardExt> queryActCardList(Long sellerId, Long userId) {
		return null;
	}

	@Override
	public SellerGoods getGoodsDetail(Long goodsSkuId, Long goodsId) {
		Query query = new Query();
		Criteria crit;
		if (goodsSkuId != null) {
			crit = Criteria.where("skuId").is(goodsSkuId);
		} else {
			crit = Criteria.where("goodsId").is(goodsId);
		}
		query.addCriteria(crit);
		SellerGoods sg = mongo.findOne(query, SellerGoods.class);
		return sg;
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
	@Override
	public List<SellerGoods> searchGoods(Long sellerId, String key,
										 Integer page, Integer size) {
		Query query = new Query();
		Seller seller = this.getSellerById(sellerId);
		Criteria crit = Criteria.where("sellerId").in(seller.getSupplierList());
		crit.and("title").regex(key);
		query.addCriteria(crit);
		query.with(new PageRequest(page - 1, size));
		List<SellerGoods> list = mongo.find(query, SellerGoods.class);
		return list;
	}

	@Override
	public List<SellerGoods> getGoodsList(Long sellerId, Long cateId,
										  Long supplierId, String sort, String type, Integer page,
										  Integer size) {
		return null;
	}

	@Override
	public ActUser getRedPackage(Long sellerId, Long userId, Long actCardId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCardId) {
		return null;
	}

	@Override
	public List<ActCardExt> supplierCardList(Long sellerId, Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActCardExt> everyDayCardList(Long sellerId, Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActCardExt getFirstRedCard(Long sellerId, Long userId) {
		// TODO Auto-generated method stub
		return null;
	}
}
