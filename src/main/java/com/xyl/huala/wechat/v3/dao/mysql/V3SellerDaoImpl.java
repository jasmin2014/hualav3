package com.xyl.huala.wechat.v3.dao.mysql;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.xyl.huala.constant.CardConst;
import com.xyl.huala.entity.*;
import com.xyl.huala.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xyl.core.jdbc.interceptor.PageControl;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.enums.SellerEnum;
import com.xyl.huala.enums.SellerGoodsEnum;
import com.xyl.huala.wechat.v3.dao.V3SellerDao;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.GoodsActGroup;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;

/**
 * @tag
 */
@Repository
@Profile("mysql")
@SuppressWarnings({})
public class V3SellerDaoImpl implements V3SellerDao {

	@Autowired
	private JdbcDao jdbcDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private V3UserDao userDao;
	@Autowired
	private JdbcOperations jdbcTemplate;
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
	public List<GeoResult<Seller>> getIndexSeller(Point gps, Integer range, PageRequest page) {
		Query query = Query
			.query(Criteria.where("isDelete").is("0").and("sellerStatus").is("0").and("sellerType").is(1));
		NearQuery near = NearQuery.near(gps).maxDistance(range, Metrics.KILOMETERS).query(query);
		near.with(page);
		GeoResults<Seller> list;
		try {
			list = mongo.geoNear(near, Seller.class);

		} catch (RuntimeException e) {
			return Collections.EMPTY_LIST;
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
	private static final String sellerSql = " SELECT a.*, b.delivery_amount, b.start_amount, b.sales,b.seller_scope  FROM h_seller a left JOIN v_seller_ext b  ON a.id = b.seller_id  where a.id=?";
	private static final String sql = "select data_value from h_seller_extends where group_key='basic_pro' and data_key = 'seller_status'  and seller_id=?";

	public Seller getSellerById(Long sellerId) {
		Seller seller = jdbcDao.queryOneBySQL(sellerSql, Seller.class, sellerId);
		String queryOneBySQL = jdbcDao.queryOneBySQL(sql, String.class, sellerId);
		// 是否开店
		seller.setSellerStatus(queryOneBySQL);
		return seller;

	}

	/**
	 * 查询cid下的类目
	 *
	 * @param seller
	 * @param cid
	 * @return
	 */
	public List<SellerGoodsCat> getCategories(Seller seller, Long cid) {
		String sql = "select a.cid,a.cname,max(cat_img) cat_img from h_seller_goods_cat a ,h_seller_extends b "
			+ " where a.seller_id=b.data_key and b.group_key='seller_supplier' and b.seller_id=? and a.parent_cid=? and a.is_delete = '0' group by a.cid";
		List<SellerGoodsCat> list = jdbcDao.queryBySQL(sql, SellerGoodsCat.class, seller.getId(), cid);
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
		String sql = "select a.id,a.name from h_seller a,h_seller_extends b where a.id=b.data_key and b.group_key='seller_supplier' and b.seller_id=?";
		List<Seller> list = jdbcDao.queryBySQL(sql, Seller.class, seller.getId());
		return list;
	}

	// 活动商品信息表
	private static final String goodsAct = "select a.id act_id,a.name,a.pic_url img_url,a.pic_img from act_info a, act_goods b,h_seller_extends c "
		+ "where a.id=b.act_id AND b.seller_id = c.data_key AND c.group_key = 'seller_supplier' "
		+ "and NOW() BETWEEN start_time and end_time and c.seller_id=?  group by a.id order by a.id desc";

	private static final String actGoodsList = "SELECT a.id, a.id goodsId, a.id sku_id, a.title, a.pic_url, a.seller_id supplier_id, a.sale_price, d.subtract, d.img_url act_img_url FROM h_seller_goods a, h_seller_extends c, act_goods d WHERE a.seller_id = c.data_key AND c.group_key = 'seller_supplier' AND a.id = d.goods_id AND d.discount IS NOT NULL AND c.seller_id =? AND d.act_id =?";

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取3个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
	public List<GoodsActGroup> getGoodsActCat(Seller seller) {
		List<GoodsActGroup> list = jdbcDao.queryBySQL(goodsAct, GoodsActGroup.class, seller.getId());
		String actList = actGoodsList + " limit 3";
		for (GoodsActGroup g : list) {
			List<SellerGoods> goodsList = jdbcDao.queryBySQL(actList, SellerGoods.class, seller.getId(), g.getActId());
			g.setGoodsList(goodsList);
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
	public List<SellerGoods> getGoodsActList(Seller seller, Long supplierId, String sort, Long actId, Integer page,
											 Integer size) {
		ActInfo actinfo = jdbcDao.get(ActInfo.class, actId);
		PageControl.performPage(page, size > 100 ? 100 : size);
		List<SellerGoods> goodsList = null;
		if (supplierId == null) {
			goodsList = jdbcDao.queryBySQL(actGoodsList + " order by d.img_url desc", SellerGoods.class, seller.getId(),
				actId);
		} else {
			goodsList = jdbcDao.queryBySQL(actGoodsList + " and c.data_key=? order by d.img_url desc",
				SellerGoods.class, seller.getId(), actId, supplierId);

		}
		for (SellerGoods s : goodsList) {
			s.setActPicUrl(actinfo.getPicUrl());
		}
		return goodsList;
	}

	/**
	 * 查找店铺的banner图
	 * @return
	 */
	public List<HSellerBanner> getSellerBannerInfo(Seller seller) {
		StringBuffer sql = new StringBuffer();
		String allBannerSql = "select * from h_seller_banner where scope_seller='all' and banner_type='user' and is_delete=0 and city=? and now() between start_time and end_time";
		//查询部分banner
		sql.append("SELECT a.id,a.banner_name,a.url,a.sort,a.img_url,a.start_time,a.end_time ")
			.append("FROM h_seller_banner a INNER JOIN h_seller_extends b ON a.id = b.data_key ")
			.append("AND b.group_key = 'seller_banner' AND b.seller_id = ? ")
			.append("WHERE a.is_delete = 0 AND a.banner_type = 'user' and now() BETWEEN a.start_time and a.end_time and a.city=?")
			.append("ORDER BY sort DESC ");
		List<HSellerBanner> queryBySQL = jdbcDao.queryBySQL(sql.toString(), HSellerBanner.class, seller.getId(),
			seller.getCity());
		//查询全场banner
		List<HSellerBanner> allBanner = jdbcDao.queryBySQL(allBannerSql, HSellerBanner.class, seller.getCity());

		if (queryBySQL != null) {
			for (HSellerBanner b : queryBySQL) {
				String url = b.getUrl();
				if (url != null && url.indexOf("sellerId") <= 0) {
					if (url.indexOf("?") > 0) {
						url += "&sellerId=" + seller.getId();
					} else {
						url += "?sellerId=" + seller.getId();
					}
				}
				b.setUrl(url);
			}
		}
		if (allBanner != null) {
			for (HSellerBanner a : allBanner) {
				String url = a.getUrl();
				if (url != null && url.indexOf("sellerId") <= 0) {
					if (url.indexOf("?") > 0) {
						url += "&sellerId=" + seller.getId();
					} else {
						url += "?sellerId=" + seller.getId();
					}
				}
				a.setUrl(url);
			}
			queryBySQL.addAll(allBanner);
		}
		return queryBySQL;
	}

	/**
	 * 商品基础信息sql
	 */
	private static final String baseGoosSql = "SELECT a.seller_id supplierId, ( SELECT d. NAME FROM h_seller d WHERE d.id = a.seller_id ) supplierName, a.id id, a.id skuId, a.title goodsName, a.id goodsId, a.cid, a.goods_sn, a.pic_url, c.discount, a.title, a.goods_status, a.rec_price, '' properties, a.sale_price, c.`name` actName, c.act_id actId, c.pic_url actPicUrl, c.subtract FROM h_seller_goods a LEFT JOIN v_act_goods c ON a.id = c.goods_id WHERE a.is_delete = '0' AND a.goods_status = '0'";
	/**
	 * 获取指定店铺对应的供应商信息
	 */
	private static final String supplierIdsSql = "select GROUP_CONCAT(data_key) from h_seller_extends where group_key='seller_supplier' and seller_id =?";
	/**
	 * 商品基础信息sql
	 */
	private static final String goodsDetail = "SELECT a.seller_id supplierId, ( SELECT d. NAME FROM h_seller d WHERE d.id = a.seller_id ) supplierName, a.id id, a.id skuId, a.title goodsName, a.id goodsId, a.cid, a.goods_sn, a.pic_url, c.discount, a.title, a.goods_status, a.goods_detail, '' properties, a.sale_price, c.`name` actName, c.act_id actId, c.pic_url actPicUrl, c.subtract FROM h_seller_goods a LEFT JOIN v_act_goods c ON a.id = c.goods_id WHERE a.is_delete = '0' AND a.goods_status = '0'";

	/**
	 * 获取商品详细信息
	 *
	 * @param goodsSkuId 商品SKUID 两个传一个便可
	 * @param goodsId    商品ID
	 * @return
	 */
	@Override
	public SellerGoods getGoodsDetail(Long goodsSkuId, Long goodsId) {

		String sql;
		SellerGoods goodsInfo;
		if (goodsSkuId != null) {
			sql = goodsDetail + " and a.id = ? ";
			goodsInfo = jdbcDao.queryOneBySQL(sql.toString(), SellerGoods.class, goodsSkuId);
		} else {
			sql = goodsDetail + " and a.id = ? ";
			goodsInfo = jdbcDao.queryOneBySQL(sql.toString(), SellerGoods.class, goodsId);
		}
		HSellerGoodsExtends h = new HSellerGoodsExtends(SellerGoodsEnum.图片地址);
		h.setGoodId(goodsInfo.getGoodsId());
		h = jdbcDao.queryOne(h);
		if (h != null && h.getDataValue() != null)
			goodsInfo.setImgUrlList(Arrays.asList(h.getDataValue().split(";")));

		goodsInfo.setGoodsDesc(this.getGoodsExt(goodsInfo.getGoodsId(), SellerGoodsEnum.宝贝描述));
		String goodsExt = this.getGoodsExt(goodsInfo.getGoodsId(), SellerGoodsEnum.销量);
		goodsInfo.setSales(Integer.parseInt(goodsExt == null ? "0" : goodsExt));
		return goodsInfo;
	}

	public String getGoodsExt(Long goodsId, SellerGoodsEnum goodsEnum) {
		HSellerGoodsExtends h = new HSellerGoodsExtends(goodsEnum);
		h.setGoodId(goodsId);
		h = jdbcDao.queryOne(h);
		if (h == null) {
			return null;
		}
		return h.getDataValue();
	}

	/**
	 * 商品搜索接口
	 */
	@Override
	public List<SellerGoods> searchGoods(Long sellerId, String key, Integer page, Integer size) {
		String supplier = jdbcTemplate.queryForObject(supplierIdsSql, String.class, sellerId);
		String searchGoodsList = baseGoosSql + " and a.seller_id in (" + supplier + ") and a.title like '%"
			+ (key == null ? "" : key) + "%'";
		PageControl.performPage(page, size);
		return jdbcDao.queryBySQL(searchGoodsList, SellerGoods.class);
	}

	/**
	 * 商品分类页面：获取商品信息
	 *
	 * @param sellerId
	 * @param cateId
	 * @param supplierId
	 * @param sort
	 * @param type       如果是1，则代表父类目，如果是
	 * @param page
	 * @param size
	 * @return
	 */
	@Override
	public List<SellerGoods> getGoodsList(Long sellerId, Long cateId, Long supplierId, String sort, String type,
										  Integer page, Integer size) {
		String sql = baseGoosSql;
		if (supplierId != null) {
			sql += " and a.seller_id =" + supplierId;
		} else {
			String supplier = jdbcTemplate.queryForObject(supplierIdsSql, String.class, sellerId);
			if (supplier != null)
				sql += " and a.seller_id in (" + supplier + ")";
			else
				sql += " and a.seller_id=" + sellerId;
		}
		if (cateId == null) {

		} else if ("1".equals(type)) {
			sql += " and a.cid in(select x.cid from h_seller_goods_cat x where x.seller_id=a.seller_id and parent_cid="
				+ (cateId == null ? 0 : cateId) + ")";
		} else {
			sql += " and a.cid =" + cateId;
		}
		if (sort != null) {// 与商品扩展关联获取商品销售，按销量排序
			sql = "select a.* from (" + sql + ") a left join h_seller_goods_extends b on"
				+ " a.goods_id=b.good_id and b.meta_key='info' and b.data_key='sales' order by b.data_value desc";
			// sql+= "order by "+sort;
		}
		PageControl.performPage(page, size > 100 ? 100 : size);
		return jdbcDao.queryBySQL(sql, SellerGoods.class);
	}

	/*************************************************************
	 * 红包处理
	 */
	/**
	 * 获取店铺中没有失效的红包信息
	 */
	private static final String find_redpacket = "select a.*,if(a.seller_id = 0, '全场',b.name) sellerName from act_card a left join h_seller b on a.seller_id=b.id  where  "
		+ " a.issue = '1' and a.type = ? and a.is_delete = '0' and NOW() BETWEEN a.start_day and a.end_time and (seller_id=? or seller_id=0)";
	/**
	 * 当前红包是否已领取过
	 */
	private static final String find_user_redpacket = "select count(1) from act_user a  where a.user_id =  ? and a.type= ? and a.status in(0,1) and now()<=end_time";

	@Override
	public ActCardExt queryOneActCard(Long sellerId, Long userId) {

		ActCardExt firstRedCard = this.getFirstRedCard(sellerId, userId);
		if (firstRedCard == null) {// 每日红包只有下过单的用户才能领取
			List<ActCardExt> perDayList = this.perDayCard(sellerId, userId);
			if (perDayList != null && perDayList.size() > 0) {
				return perDayList.get(0);
			} else {
				return null;
			}
		}
		return firstRedCard;
	}

	/**
	 * 店铺首页
	 * <p>
	 * 查询可使用红包列表
	 *
	 * @param sellerId
	 * @param userId
	 * @return
	 */

	private static final String FIND_SUPPLIER_RED_PACKAGE = "select * from act_card where seller_id=0 and type=4 and now() between  start_day and end_time and is_delete='0'and id not in(select act_cart_id from act_user where user_id=? and seller_id=0 and type=4);";

	@Override
	public List<ActCardExt> supplierCardList(Long sellerId, Long userId) {
		// 查询关联供应商红包
		List<ActCardExt> supplierActCardList = new ArrayList<ActCardExt>();
		supplierActCardList = jdbcDao.queryBySQL(FIND_SUPPLIER_RED_PACKAGE, ActCardExt.class, userId);
		if (supplierActCardList != null && supplierActCardList.size() > 0) {
			for (ActCardExt actCardExt : supplierActCardList) {
				actCardExt.setSellerName("仅供应商可用");
			}
		}
		return supplierActCardList;
	}

	@Override
	public List<ActCardExt> everyDayCardList(Long sellerId, Long userId) {
		// 每日红包:下过单的用户才能领取
		List<ActCardExt> actCardExt = this.perDayCard(sellerId, userId);
		return actCardExt;
	}

	/**
	 * 首单红包
	 *
	 * @param sellerId
	 * @return
	 */
	@Override
	public ActCardExt getFirstRedCard(Long sellerId, Long userId) {
		// 每日红包:下过单的用户才能领取
		if (!checkFirst(sellerId))
			return null;
		int count = userDao.getUserOrderCount(userId);
		ActCardExt actCardExt1 = jdbcDao.queryOneBySQL(find_redpacket,
			ActCardExt.class, "1", sellerId);
		// 查询是否领取过
		if (actCardExt1 == null) {
			return null;
		}
		int userCardCount = jdbcDao.queryOneBySQL(find_user_redpacket,
			Integer.class, userId, actCardExt1.getType());
		if (count <= 0 && userCardCount <= 0) {
			return actCardExt1;
		}
		return null;
	}

	/**
	 * 判断首单是否可领
	 *
	 * @param sellerId
	 * @return
	 */
	private boolean checkFirst(Long sellerId) {
		Integer haveGetNum = 0;
		final Object o = redisDao.get(CardConst.seller_first_card_have_get + sellerId);
		if (o != null) {
			haveGetNum = Integer.parseInt(o.toString());
		}
		final Integer sellerCardLimit = this.getSellerCardLimit(sellerId);
		if (haveGetNum >= sellerCardLimit) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 店铺红包的限制数量
	 *
	 * @param sellerId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Integer getSellerCardLimit(Long sellerId) {
		Object o = redisDao.get(CardConst.seller_first_card_limit + sellerId);
		if (o != null) {
			return Integer.parseInt(o.toString());
		}
		HSellerExtends entity = new HSellerExtends(SellerEnum.FIRST_CARD_LIMIT);
		entity.setSellerId(sellerId);
		HSellerExtends se = jdbcDao.queryOne(entity);
		if (se != null) {
			redisDao.set(CardConst.seller_first_card_limit + sellerId, se.getDataValue(), DateUtils.getMiao(), TimeUnit.SECONDS);
			return Integer.parseInt(se.getDataValue());
		} else {
			redisDao.set(CardConst.seller_first_card_limit + sellerId, "100", DateUtils.getMiao(), TimeUnit.SECONDS);
			return 100;
		}
	}

	/**
	 * 每日红包领取，如果新手红包没有，或已领取，则展现每日红包
	 *
	 * @param sellerId
	 * @param userId
	 * @return
	 */
	private static final String find_perday = "select a.*,if(a.seller_id=0,'全场','') sellerName from act_card a where a.issue = '1' "
		+ "AND a.type = 3 AND a.is_delete = '0' and a.card_num_limit = - 1 AND NOW() BETWEEN a.start_day AND a.end_time and a.city=?";

	private List<ActCardExt> perDayCard(Long sellerId, Long userId) {
		HSeller seller = jdbcDao.get(HSeller.class, sellerId);
		Object o = redisDao.get("card_get:" + userId);
		if (o != null) {// 新手红包，如果没有领取过，则返回新手红包，如果领取过，则获取每日红包
			return null;
		}
		// 判断今日是否领取过红包
		Integer count = this.isGetRedPackage(userId);
		if (count != null && count > 0)
			return null;
		List<ActCardExt> actCard = jdbcDao.queryBySQL(find_perday, ActCardExt.class, seller.getCity());
		if (actCard != null && actCard.size() > 0) {
			return actCard;
		}
		return null;
	}

	/**
	 * 获取用户今日是否领取过每日红包
	 */
	private static final String isGetRedPackage = "select count(1) from act_user where user_id = ? and type = 3 and add_time  between DATE_FORMAT(now(),'%Y-%m-%d 00:00:00') and DATE_FORMAT(now(),'%Y-%m-%d 23:59:59')";

	public Integer isGetRedPackage(Long userId) {
		return jdbcDao.queryOneBySQL(isGetRedPackage, Integer.class, userId);
	}

	/**
	 * 领取红包
	 *
	 * @param sellerId 店铺ID
	 * @param userId   用户ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public ActUser getRedPackage(Long sellerId, Long userId, Long actCardId) {
		ActCard actCard = jdbcDao.get(ActCard.class, actCardId);
		int limit = actCard.getCardSurplus();
		if (limit <= 0 && actCard.getCardNumLimit() > 0) {
			return null;
		}
		int count = userDao.getUserOrderCount(userId);
		if (count <= 0 && !checkFirst(sellerId) && actCard.getType() == 1) {
			// 首单 如果用户领取完了 不能使用
			return null;
		}
		// ActInfo actInfo = jdbcDao.get(ActInfo.class, actCard.getActId());
		// actCard.setCardSurplus(--limit);
		jdbcDao.update(new com.xyl.core.jdbc.persistence.Criteria(ActCard.class)
			.set("card_surplus", --limit)
			.where("id", actCard.getId()));
		ActUser a = this.getRedPackage(sellerId, userId, actCard);
		if (a != null && a.getId() == null) {
			// 红包已经领取并且使用了
			return null;
		}
		if (a == null) {
			// 没有领取过红包
			a = new ActUser();
			a.setActCartId(actCard.getId());
			a.setUserId(userId);
			a.setName(actCard.getName());
			a.setSums(actCard.getSums());
			a.setBalance(actCard.getBalance());
			Date date = new Date();
			a.setStartDay(date);
			Date endd = DateUtils.addDays(date, actCard.getValidDays() - 1);
			endd = DateUtils.getDateDetailTime(DateUtils.date(endd, "yyyy-MM-dd") + " 23:59:59");
			if (endd.before(actCard.getEndTime())) {
				a.setEndTime(endd);
			} else {
				a.setEndTime(actCard.getEndTime());
			}
			a.setAddTime(date);
			a.setSellerId(actCard.getSellerId());
			a.setType(actCard.getType());
			a.setPlatformPrice(actCard.getPlatformPrice());
			Long actUserId = jdbcDao.insert(a);
			a.setId(actUserId);
		}
		/**
		 * 同一个店铺，一次只能领取一个
		 */
		int timeout = DateUtils.getMiao();
		if (actCard.getType() == 1) {
			Integer haveGetNum = 0;
			Object o = redisDao.get(CardConst.seller_first_card_have_get + sellerId);
			if (o != null) {
				haveGetNum = Integer.parseInt(o.toString());
			}
			redisDao.set(CardConst.seller_first_card_have_get + sellerId, haveGetNum + 1, timeout, TimeUnit.SECONDS);
		} else if (actCard.getType() == 3) {// 每日红包
			redisDao.set("card_get:" + userId, actCardId + "当日已领取", timeout, TimeUnit.SECONDS);
		}
		return a;
	}

	/**
	 * 判断用户是否可以领取红包 返回空 代表没有领取过
	 */
	private ActUser getRedPackage(Long sellerId, Long userId, ActCard actCard) {
		int index = 0;
		if (actCard.getType() == 1) {
			// 如果是首单 则判断用户是否领取过
			ActUser a = new ActUser();
			a.setActCartId(actCard.getId());
			a.setUserId(userId);
			a.setType(1);
			// 该用户已经领取过该红包 则不能重复领取
			List<ActUser> cardList = jdbcDao.queryList(a);
			if (cardList != null && cardList.size() > 0) {
				for (ActUser au : cardList) {
					index++;
					if ("0".equals(au.getStatus())) {
						return au;
					}
				}
			}
		} else if (actCard.getType() == 3) {
			// 如果是每日红包 则判断用户今日是否领取过
			List<ActUser> cardList = userDao.getRedPackage(userId);
			if (cardList != null && cardList.size() > 0) {
				for (ActUser au : cardList) {
					index++;
					if ("0".equals(au.getStatus())) {
						return au;
					}
				}
			}
		}
		// 领取过 已经使用了
		if (index > 0)
			return new ActUser();
		return null;
	}

	/**
	 * 店铺首页
	 * <p>
	 * 一键领取红包集合
	 *
	 * @param sellerId
	 * @param userId
	 * @param actCardId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCardId) {
		List<ActUser> actUserList = new ArrayList<ActUser>();
		if (null != actCardId && actCardId.size() > 0) {

			for (String actCartIdItem : actCardId) {
				ActCard actCard = jdbcDao.get(ActCard.class, Long.valueOf(actCartIdItem));
				// ActInfo actInfo = jdbcDao.get(ActInfo.class, actCard.getActId());
				if ("1".equals(actCard.getType())) {
					String sql2 = "select count(1) from act_user where type=1 and NOW() BETWEEN start_day and end_time and `status` in ('0','1')  and user_id=?";
					Integer count = jdbcDao.queryCount(sql2, userId);
					if (count > 0) {// 如果首单红包其他店铺已领取过，则不允许在领取
						continue;
						// jdbcDao.update(actCard);
					}
				}
				ActUser a = new ActUser();
				a.setName(actCard.getName());
				a.setActCartId(actCard.getId());
				a.setSums(actCard.getSums());
				a.setBalance(actCard.getBalance());
				a.setContent(actCard.getContent());
				Date date = new Date();
				a.setStartDay(date);
				Date endd = DateUtils.addDays(date, actCard.getValidDays() - 1);
				endd = DateUtils.getDateDetailTime(DateUtils.date(endd, "yyyy-MM-dd") + " 23:59:59");
				if (endd.before(actCard.getEndTime())) {
					a.setEndTime(endd);
				} else {
					a.setEndTime(actCard.getEndTime());
				}
				a.setAddTime(date);
				a.setUserId(userId);
				a.setSellerId(actCard.getSellerId());
				a.setType(actCard.getType());
				a.setPlatformPrice(actCard.getPlatformPrice());
				Long actUserId = jdbcDao.insert(a);
				a.setId(actUserId);
				actUserList.add(a);
				/**
				 * 同一个店铺，一次只能领取一个
				 */
				int timeout = DateUtils.getMiao();
				if (actCard.getType() == 1) {
					Integer haveGetNum = 0;
					Object o = redisDao.get(CardConst.seller_first_card_have_get + sellerId);
					if (o != null) {
						haveGetNum = Integer.parseInt(o.toString());
					}
					redisDao.set(CardConst.seller_first_card_have_get + sellerId, haveGetNum + 1, timeout, TimeUnit.SECONDS);
				} else if (actCard.getType() == 3) {// 每日红包
					redisDao.set("card_get:" + userId, actCartIdItem + "当日已领取", timeout, TimeUnit.SECONDS);
				}

			}
		}
		return actUserList;
	}
}
