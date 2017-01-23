/**
 *
 */
package com.xyl.huala.wechat.v3.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;

import com.xyl.huala.domain.Gps;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HSellerBanner;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.GoodsActGroup;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;

/**
 * @author leazx
 */
public interface V3SellerService {
    /**
     * 获取微信定位的GPS
     *
     * @param openid
     * @return
     */
    public Gps getLocation(String openid);

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
     * 根据经纬度查询最近的商店
     *
     * @param lng
     * @param lat
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Seller getSellerByGps(Gps gps);

    /**
     * 查询cid下的类目
     *
     * @param sid
     * @param cid
     * @return
     */
    public List<SellerGoodsCat> getCategories(Long sid);

    /**
     * 获取某个店铺的供应商列表
     *
     * @param seller
     * @return
     */
    public List<Seller> getSupplier(Long sid);

    /**
     * @param sellerId
     * @param type
     * @param supplierId
     * @param page
     * @return
     */
    public List<SellerGoods> getGoodsList(Long sellerId, Long cateId,
                                          Long supplierId, String sort, String type, Integer page,
                                          Integer size);

    /**
     * 获取商品详细信息
     *
     * @param goodsSkuId 商品SKUID 两个传一个便可
     * @param goodsId    商品ID
     * @return
     */
    public SellerGoods getGoodsDetail(Long goodsSkuId, Long goodsId);

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
     * 获取店铺商品活动信息<br>
     * 按照活动分组，每一每获取6个商品，组成活动列表
     *
     * @param seller
     * @return
     */
    public List<HSellerBanner> getBanner(Seller seller);

    /**
     * 店铺首页，是否有红包是否为首单红包，如果是首单则显示，不是首单则不显示
     *
     * @param sellerId 店铺ID
     * @param userId   用户ID
     * @return
     */
    public ActCardExt firstRedPackage(Long sellerId, Long userId);


    /**
     * 店铺首页红包列表
     * <p>
     * 包含{ 首单红包  新手红包   活动红包 }
     *
     * @param sellerId
     * @param userId
     * @return
     */
    public List<ActCardExt> homeRedPackage(Long sellerId, Long userId);

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
     * @param actCartId
     * @return
     */
    public List<ActUser> getRedPackageList(Long sellerId, Long userId, List<String> actCartId);

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
                                           Integer page, Integer size);

    /**
     * 增加店铺的今日访客数
     */
    public void addSellerVisitorNumber(Long sellerId, Long userId);
}
