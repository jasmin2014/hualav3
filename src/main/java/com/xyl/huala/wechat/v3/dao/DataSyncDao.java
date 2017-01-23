package com.xyl.huala.wechat.v3.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.HSellerExtends;
import com.xyl.huala.entity.HSellerGoodsExtends;
import com.xyl.huala.enums.SellerEnum;
import com.xyl.huala.enums.SellerGoodsEnum;
import com.xyl.huala.wechat.v3.domain.GoodsAct;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;
import com.xyl.huala.wechat.v3.util.PropUtil;

@Repository
public class DataSyncDao {
	private static final Logger logger = Logger.getLogger(DataSyncDao.class);
	@Autowired
	private JdbcDao jdbcDao;

	public List<Seller> getSellerByGps() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * from h_seller");
		return jdbcDao.queryBySQL(sb.toString(), Seller.class);
	}

	/**
	 * 查询店铺满减信息 (以列表形式展现)
	 * 
	 * @param sid
	 * @return
	 */
	private final static String QUERY_SELLER_COUPON = "select  b.content FROM 	act_info a,	act_rule b,	act_seller c"
			+ " WHERE c.act_id = a.id AND a.id = b.act_id and c.seller_id = ? ORDER BY b.sums desc ";

	public List<String> getSellerCoupon(Long sid) {
		return jdbcDao.queryBySQL(QUERY_SELLER_COUPON, String.class, sid);
	}

	public Seller getSellerByGpsById(Long sellerId) {
		StringBuffer sb = new StringBuffer();
		sb.append(
				" SELECT a.*, b.delivery_amount, b.start_amount, b.sales,b.seller_scope")
				.append(" FROM h_seller a left JOIN v_seller_ext b")
				.append(" ON a.id = b.seller_id  where a.id=?");
		return jdbcDao.queryOneBySQL(sb.toString(), Seller.class, sellerId);
	}

	/**
	 * 获取店铺对应的供应商
	 * 
	 * @param sellerId
	 * @return
	 */
	public List<Long> getSupplierList(Long sellerId) {
		HSellerExtends entity = new HSellerExtends(SellerEnum.SELLER_SUPPLIER);
		entity.setSellerId(sellerId);
		List<HSellerExtends> list = jdbcDao.queryList(entity);
		List<Long> ret = new ArrayList<>();
		for (HSellerExtends a : list) {
			ret.add(Long.parseLong(a.getDataKey()));
		}
		return ret;
	}

	/**
	 * 获取扩展属性值
	 * 
	 * @param sellerEnum
	 * @return
	 */
	public String getExt(Long sellerId, SellerEnum sellerEnum) {
		HSellerExtends entity = new HSellerExtends(sellerEnum);
		entity.setSellerId(sellerId);
		entity = jdbcDao.queryOne(entity);
		return entity == null ? null : entity.getDataValue();
	}

	/**
	 * 獲取商品的活动信息,传入商品skuID
	 */
	private static final String actgoods = "SELECT a.*,b.act_id,b.discount,b.subtract "
			+ "FROM act_info a, act_goods b "
			+ "where a.id=b.act_id and now() BETWEEN a.start_time and a.end_time and b.goods_sku_id=?";

	public GoodsAct getGoodsAct(Long skuId) {
		return jdbcDao.queryOneBySQL(actgoods, GoodsAct.class, skuId);
	}

	/**
	 * 获取商品详细信息
	 * 
	 * @param goodsId
	 * @return
	 */
	public SellerGoods getGoodDetailInfo(Long sid, Long goodsId) {
		SellerGoods sellerGoods = this.getGoodsDetail(sid, goodsId);
		try {
			List<HSellerGoodsExtends> propList = this.getGoodsProp(goodsId);
			// 产品信息之类的属性
			sellerGoods.setInformation(PropUtil.toPropMap(propList));
			HSellerGoodsExtends sge = this.getGoodsExt(goodsId,
					SellerGoodsEnum.销量);
			if (sge != null) {
				sellerGoods.setSales(Integer.parseInt(sge.getDataValue()));
			}
			sge = this.getGoodsExt(goodsId, SellerGoodsEnum.图片地址);
			if (sge != null) {
				List<String> imgUrlList = Arrays.asList(sge.getDataValue().split(";"));
				sellerGoods.setImgUrlList(imgUrlList);
			}
			sge=this.getGoodsExt(goodsId, SellerGoodsEnum.宝贝描述);
			sellerGoods.setGoodsDesc(sge.getDataValue());
		} catch (Exception e) {
			logger.error(e);
		}
		return sellerGoods;
	}

	/**
	 * 获取
	 * 
	 * @param goodsId
	 * @return
	 */
	public SellerGoods getGoodsDetail(Long sid, Long goodsId) {
		StringBuffer sql = new StringBuffer();
//		sql.append("SELECT	a.seller_id supplierId ,e.data_value supplierName,? seller_id,b.id id,b.id skuId,a.title goodsName, ");
//		sql.append("b.goods_id,	a.cid,a.goods_sn,a.pic_url,b.sale_price oriPrice,c.discount,a.title,a.goods_status, ");
//		sql.append("b.properties,IF (c.discount IS NOT NULL,b.sale_price - c.subtract,	b.sale_price) sale_price,  ");
//		sql.append("c.`name` actName, c.act_id actId,c.pic_url actPic,IF (e.id IS NOT NULL AND a.is_delete = '0'	AND a.goods_status = '0',1,0) STATUS ");
//		sql.append("FROM h_seller_goods a ");
//		sql.append("INNER JOIN h_seller_sku_prop b ON a.id = b.goods_id ");
//		sql.append("LEFT JOIN v_act_goods c ON b.id = c.goods_sku_id ");
//		sql.append("LEFT JOIN h_seller_extends e ON e.seller_id = ? ");
//		sql.append("AND e.group_key = 'seller_supplier' AND data_key = a.seller_id ");
//		sql.append("WHERE a.id = ? ");
		sql.append("SELECT a.seller_id supplierId, e.data_value supplierName,? seller_id, a.id id, a.id skuId, a.title goodsName, a.id goods_id, a.cid, a.goods_sn, a.pic_url, a.sale_price oriPrice, c.discount, a.title, a.goods_status, '' properties, IF ( c.discount IS NOT NULL, a.sale_price - c.subtract, a.sale_price) sale_price, c.`name` actName, c.act_id actId, c.pic_url actPic, IF ( e.id IS NOT NULL AND a.is_delete = '0' AND a.goods_status = '0', 1, 0) STATUS FROM h_seller_goods a LEFT JOIN v_act_goods c ON b.id = c.goods_sku_id LEFT JOIN h_seller_extends e ON e.seller_id = ? AND e.group_key = 'seller_supplier' AND data_key = a.seller_id WHERE a.id = ?");
		return jdbcDao.queryOneBySQL(sql.toString(), SellerGoods.class, sid,
				sid, goodsId);
	}

	/**
	 * 获取属性列表
	 * 
	 * @param goodsId
	 * @return
	 */
	public List<HSellerGoodsExtends> getGoodsProp(Long goodsId) {
		HSellerGoodsExtends hSellerGoodsExtends = new HSellerGoodsExtends(
				SellerGoodsEnum.商品属性);
		hSellerGoodsExtends.setGoodId(goodsId);
		return jdbcDao.queryList(hSellerGoodsExtends);
	}

	/**
	 * 获取指定的属性
	 * 
	 * @param goodsId
	 * @param sellerGoodsEnum
	 *            枚举类型，用来指定查询条件
	 * @return
	 */
	public HSellerGoodsExtends getGoodsExt(Long goodsId,
			SellerGoodsEnum sellerGoodsEnum) {
		HSellerGoodsExtends hSellerGoodsExtends = new HSellerGoodsExtends(
				sellerGoodsEnum);
		hSellerGoodsExtends.setGoodId(goodsId);
		return jdbcDao.queryOne(hSellerGoodsExtends);
	}

	/**
	 * 从数据库中获取分类列表
	 * 
	 * @param sellerId
	 * @return
	 */
	public List<SellerGoodsCat> getAllGoodsCats(Long sellerId) {
		if (sellerId != null) {
			String sql = "select * from h_seller_goods_cat where seller_id=?";
			return jdbcDao.queryBySQL(sql, SellerGoodsCat.class, sellerId);
		} else {
			String sql = "select * from h_seller_goods_cat";
			return jdbcDao.queryBySQL(sql, SellerGoodsCat.class);
		}
	}
}
