package com.xyl.huala.wechat.v3.dao;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;

import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerBanner;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.GoodsActGroup;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;

/**
 * @tag
 */
public interface V3SellerDao {

	/**
	 * 周边看看数据
	 *
	 * @param gps
	 * @param range
	 * @param page
	 * @return
	 */
	public List<GeoResult<Seller>> getIndexSeller(Point gps, Integer range,
												  PageRequest page);

	/**
	 * 根据店铺ID取出商家信息
	 *
	 * @param lng
	 * @param lat
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public Seller getSellerById(Long sid);

	/**
	 * 查询cid下的类目
	 *
	 * @param sid
	 * @param cid
	 * @return
	 */
	// @Cacheable(value="goods",key="'V3SellerDao.getCategories('+#seller.id+','+#cid+')'")
	public List<SellerGoodsCat> getCategories(Seller seller, Long cid);

	/**
	 * 获取某个店铺的供应商列表
	 *
	 * @param seller
	 * @return
	 */
	public List<Seller> getSupplier(Seller seller);

	/**
	 * 获取店铺商品活动信息<br>
	 * 按照活动分组，每一每获取3个商品，组成活动列表
	 *
	 * @param seller
	 * @return
	 */
	public List<GoodsActGroup> getGoodsActCat(Seller seller);

	/**
	 * 获取活动商品列表
	 *
	 * @param seller 店铺信息
	 * @param actId  活动ID
	 * @return
	 */
	public List<SellerGoods> getGoodsActList(Seller seller, Long supplierId,
											 String sort, Long actId, Integer page, Integer size);

	/**
	 * 查找店铺的banner图
	 *
	 * @param sellerId
	 * @param bannerType user 用户端显示 or seller 商户端显示
	 * @return
	 */
	public List<HSellerBanner> getSellerBannerInfo(Seller seller);

	/**
	 * 获取店铺中没有失效的红包信息
	 * <p>
	 * 当前红包是否已领取过
	 */

	public ActCardExt queryOneActCard(Long sellerId, Long userId);

	/**
	 * 供应商红包
	 *
	 * @param sellerId
	 * @param userId
	 * @return
	 */
	public List<ActCardExt> supplierCardList(Long sellerId, Long userId);
	/**
	 * 每日红包
	 *
	 * @param sellerId
	 * @param userId
	 * @return
	 */
	public List<ActCardExt> everyDayCardList(Long sellerId, Long userId);
	/**
	 * 首单红包
	 * @param sellerId
	 * @return
	 */
	public ActCardExt getFirstRedCard(Long sellerId,Long userId);
	/**
	 * 获取商品详细信息
	 *
	 * @param goodsSkuId 商品SKUID 两个传一个便可
	 * @param goodsId    商品ID
	 * @return
	 */
	public SellerGoods getGoodsDetail(Long goodsSkuId, Long goodsId);

	/**
	 * 搜索商品
	 *
	 * @param sellerId
	 * @param key
	 * @param page
	 * @param size
	 * @return
	 */
	public List<SellerGoods> searchGoods(Long sellerId, String key,
										 Integer page, Integer size);

	/**
	 * 商品分类页面：获取商品信息
	 *
	 * @param sellerId
	 * @param cateId
	 * @param supplierId
	 * @param sort
	 * @param type
	 * @param page
	 * @param size
	 * @return
	 */
	public List<SellerGoods> getGoodsList(Long sellerId, Long cateId,
										  Long supplierId, String sort, String type, Integer page,
										  Integer size);

	/**
	 * 领取红包
	 *
	 * @param sellerId 店铺ID
	 * @param userId   用户ID
	 * @return
	 */
	public ActUser getRedPackage(Long sellerId, Long userId, Long actCardId);

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
	public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCardId);
}
