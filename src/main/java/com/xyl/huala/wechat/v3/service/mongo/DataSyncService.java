package com.xyl.huala.wechat.v3.service.mongo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.entity.HSellerGoods;
import com.xyl.huala.enums.SellerEnum;
import com.xyl.huala.wechat.v3.dao.DataSyncDao;
import com.xyl.huala.wechat.v3.domain.GoodsAct;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;
import com.xyl.huala.wechat.v3.web.V3UserWeb;

@Service
public class DataSyncService {
	private static final Logger logger = LoggerFactory.getLogger(V3UserWeb.class);
	@Autowired
	private JdbcDao jdbcDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisOperations redisTemplate;
	@Autowired
	private MongoTemplate mongo;
	@Autowired
	private DataSyncDao dataSyncDao;

	/**
	 * 同步店铺信息<br>
	 * 将mysql的店铺信息同步到mongodb中，类型为seller实体
	 */
	public void insertSeller(Long sellerId) {
		if(sellerId==null){
			List<Seller> list = dataSyncDao.getSellerByGps();
			for (Seller l : list) {
				this.insertSellerById(l.getId());
			}
			IndexOperations indexOps = mongo.indexOps(Seller.class);
			indexOps.ensureIndex(new GeospatialIndex("gps"));
		}else{
			this.insertSellerById(sellerId);
		}
	}

	/**
	 * 批量同步商品信息
	 */
	public void syncGoods(Long goodsId) {
		HSellerGoods entity = new HSellerGoods();
		entity.setId(goodsId);
		List<HSellerGoods> list = jdbcDao.queryList(entity);
		for (HSellerGoods l : list) {
			SellerGoods s = dataSyncDao.getGoodDetailInfo(l.getSellerId(),
					l.getId());
			if (s != null) {
				/**
				 * 商品活动处理
				 */
				GoodsAct g = dataSyncDao.getGoodsAct(s.getSkuId());
				//s.setAct(g);
				mongo.save(s);
			}
		}
		IndexOperations indexOps = mongo.indexOps(SellerGoods.class);
		indexOps.ensureIndex(new Index("sellerId", Direction.ASC));
		indexOps.ensureIndex(new Index("skuId", Direction.ASC).unique());
		indexOps.ensureIndex(new Index("supplierId", Direction.ASC));
	}

	/**
	 * 批量同步商品分类信息
	 */
	public void syncGoodsCat(Long sellerId) {
		List<SellerGoodsCat> list = dataSyncDao.getAllGoodsCats(sellerId);
		mongo.insertAll(list);
	}

	/**
	 * 根据店铺id 将mysql数据同步到mongodb中
	 * 
	 * @param sid
	 *            店铺id
	 */
	public void insertSellerById(Long sellerId) {
		Seller seller = dataSyncDao.getSellerByGpsById(sellerId);
		if (seller != null) {
			String ext = dataSyncDao.getExt(sellerId, SellerEnum.SELLER_STATUS);
			seller.setSellerStatus(StringUtils.isNotBlank(ext) ? ext : "0");
			List<String> couponList = dataSyncDao.getSellerCoupon(sellerId);
			seller.setCouponList(couponList);
			if (seller.getLng() != null) {
				seller.setGps(new Double[] { seller.getLng().doubleValue(),
						seller.getLat().doubleValue() });
			}
			seller.setSupplierList(dataSyncDao.getSupplierList(sellerId));
			mongo.save(seller);
		} else {
			mongo.remove(Query.query(Criteria.where("id").is(sellerId)),
					Seller.class);
		}
	}

	/**
	 * 定时同步店铺信息
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Scheduled(fixedDelay = 30 * 1000)
	public void syncSeller() {
		int count=0;
		SetOperations opset = redisTemplate.opsForSet();
		do {
			Object sellerId =  opset.pop("sync_seller");
			if(sellerId==null){
				break;
			}
			if(sellerId instanceof Long){
					insertSellerById((Long)sellerId);
					count++;
			}
			if(sellerId instanceof String){
				insertSellerById(Long.parseLong((String)sellerId));
				count++;
		}
		} while (true);
		if(count>0)
		logger.info("本次同步店铺信息"+count+"条");
	}

	/**
	 * 定时同步商品信息
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Scheduled(fixedDelay = 10 * 1000)
	public void syncGoods() {
		int count=0;
		SetOperations opset = redisTemplate.opsForSet();
		do {
			Object goodsId = opset.pop("sync_goods");
			if(goodsId==null){
				break;
			}
			if (goodsId instanceof Long) {
				syncGoods((Long)goodsId);
				count++;
			}
			if (goodsId instanceof String) {
				syncGoods(Long.parseLong((String)goodsId));
				count++;
			}
		} while (true);
		if(count>0)
		logger.info("本次同步商品信息"+count+"条");
	}

}
