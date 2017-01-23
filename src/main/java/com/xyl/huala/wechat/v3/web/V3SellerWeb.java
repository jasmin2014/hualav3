package com.xyl.huala.wechat.v3.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyl.huala.entity.ActUser;
import com.xyl.huala.wechat.v3.config.Authentication;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Place;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;
import com.xyl.huala.wechat.v3.service.V3SellerService;
import com.xyl.huala.wechat.v3.util.WxSession;

/**
 * 购物车信息
 *
 * @author wyl0153
 */
@Controller
@RequestMapping("v3")
public class V3SellerWeb {
	@Autowired
	private V3SellerService sellerService;

	/****************************************************************************
	 *
	 *
	 * 周边看看，通过定位获取将其设置为cookie，如果cookie没有则定位获取<br>
	 * 店铺信息：店铺信息展现店铺的列表，分类信息 店铺详情：商店的详细信息
	 *
	 ************************************************************************/
	/**
	 * 店铺信息 gps的cokice必须存在，如果不存在则，页面上直接定位取得
	 *
	 * @param sellerId
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@RequestMapping(value = "/seller", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<Map> getSellerInfo(Place gps,
									  Long sellerId, HttpServletResponse response) {
		DataRet<Map> ret = new DataRet<Map>();
		Map m = new HashMap<>();

		Seller seller = null;
		Place place = gps;
		if (place.getLat() == null) {
			place.setLat(0d);
			place.setLng(0d);
		}
		if (sellerId == null) {
			if (place.getSellerId() == null || place.getSellerId() == 0) {
				seller = sellerService.getSellerByGps(place);
			} else {
				seller = sellerService.getSellerById(place.getSellerId());
			}
		} else {
			seller = sellerService.getSellerById(sellerId);
		}
		if (seller != null) {
			m.put("seller", seller);
			m.put("cat", sellerService.getCategories(seller.getId()));
			m.put("actList", sellerService.getGoodsActCat(seller));
			m.put("banner", sellerService.getBanner(seller));
			if (place.getLat() == 0d) {
				place.setLat(seller.getLat().doubleValue());
				place.setLng(seller.getLng().doubleValue());
				place.setAddress(seller.getAddress());
			}
			place.setSellerId(sellerId);
			m.put("gps", place);
			// setSellertToCookie(place, response);
			ret.setBody(m);
			// 增加店铺的今日访客数
			sellerService.addSellerVisitorNumber(seller.getId(), WxSession.getUserId());
		}

		return ret;
	}

	/**
	 * 获取商家信息详情
	 *
	 * @param sellerId
	 * @return
	 */
	@RequestMapping(value = "/seller/detail/{sellerId}", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<Seller> getSellerDetial(@PathVariable Long sellerId) {
		DataRet<Seller> ret = new DataRet<Seller>();

		ret.setBody(sellerService.getSellerById(sellerId));

		return ret;
	}

	/**
	 * 周边看看数据接口
	 * @param gps
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value = "seller/list", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<List<GeoResult<Seller>>> loadIndexSeller(Place gps,
															Integer page, Integer size) {
		DataRet<List<GeoResult<Seller>>> ret = new DataRet<List<GeoResult<Seller>>>();
		if (gps == null || gps.getLat() == null) {
			ret.setErrorCode("no_gps");
			ret.setMessage("没有GPS");
			return ret;
		}
		Place g = gps;
		Point p = new Point(g.getLng(), g.getLat());
		// 查询出附近的商家
		List<GeoResult<Seller>> list = sellerService.getIndexSeller(p, 3,
			new PageRequest(page - 1, size));
		ret.setBody(list);
		return ret;
	}

	/****************************************************************************
	 *
	 *
	 * 商品信息，商品分類列表，商品信息详情
	 *
	 ************************************************************************/
	/**
	 * 商品详情页面
	 * @param sellerId
	 * @param goodsId
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@RequestMapping(value = "goods/{sellerId}/{goodsId}", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<Map> getSellerGoodsDetail(
		@PathVariable("sellerId") Long sellerId,
		@PathVariable("goodsId") Long goodsId) {
		DataRet<Map> ret = new DataRet<Map>();
		// 查询出附近的商家
		SellerGoods goods = sellerService.getGoodsDetail(null, goodsId);
		Seller seller = sellerService.getSellerById(sellerId);
		Map map = new HashMap<>();
		map.put("goods", goods);
		map.put("seller", seller);
		ret.setBody(map);
		return ret;
	}

	/**
	 * 店铺分类主页面
	 *
	 * @param sellerId   商户ID
	 * @param cateId     分类ID ，如果type类型为-1，则分类类目为活动的ID
	 * @param supplierId 供应商ID
	 * @param sort       排序方式
	 * @param type       类型，1为父类目,-1为活动
	 * @param page       页码
	 * @param size       每页数量
	 * @return
	 */
	@RequestMapping(value = "goods/{sellerId}/list", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<List<SellerGoods>> getSellerGoods(
		@PathVariable("sellerId") Long sellerId, Long cateId,
		Long supplierId, String sort, String type, Integer page,
		Integer size) {
		DataRet<List<SellerGoods>> ret = new DataRet<List<SellerGoods>>();
		if (type != null && "-1".equals(type)) {//如果type类型为-1，则分类类目为活动的ID
			Seller seller = sellerService.getSellerById(sellerId);
			List<SellerGoods> list = sellerService.getGoodsActList(seller, supplierId, sort, cateId, page, size);
			ret.setBody(list);
			return ret;
		}
		List<SellerGoods> list = sellerService.getGoodsList(sellerId, cateId,
			supplierId, sort, type, page, size);
		ret.setBody(list);
		return ret;
	}

	/**
	 * 店铺商品分类页面
	 * @param sellerId
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@RequestMapping(value = "goods/{sellerId}/cat", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<Map> getSellerGoodsCat(
		@PathVariable("sellerId") Long sellerId) {
		DataRet<Map> ret = new DataRet<Map>();
		// 查询出附近的商家
		List<SellerGoodsCat> list = sellerService.getCategories(sellerId);
		Map map = new HashMap<>();
		map.put("cat", list);
		map.put("supplier", sellerService.getSupplier(sellerId));
		ret.setBody(map);
		return ret;
	}

	/**
	 * 店铺分类主页面
	 * @param sellerId
	 * @param key
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value = "goods/{sellerId}/search", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<List<SellerGoods>> searchGoods(
		@PathVariable("sellerId") Long sellerId, String key, Integer page,
		Integer size) {
		DataRet<List<SellerGoods>> ret = new DataRet<List<SellerGoods>>();
		List<SellerGoods> list = sellerService.getSearchList(sellerId, key,
			page, size);
		ret.setBody(list);
		return ret;
	}


	/**
	 * 新版  红包领取首页使用
	 * <p>
	 * 红包列表包含{ 新手红包  首单红包  活动红包 }
	 *
	 * @param sellerId
	 * @return
	 */
	@SuppressWarnings({})
	@RequestMapping(value = "homeRedPackage/{sellerId}", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<List<ActCardExt>> homeRedPackage(@PathVariable("sellerId") Long sellerId) {
		DataRet<List<ActCardExt>> ret = new DataRet<List<ActCardExt>>();
		Long userId = WxSession.getUserId();
		if (userId == null) {
			ret.setErrorCode("no_login");
			ret.setMessage("没有登录");
			return ret;
		}
		List<ActCardExt> cardExtList = sellerService.homeRedPackage(sellerId, userId);
		if (cardExtList.size() == 0 || cardExtList == null) {
			ret.setErrorCode("no_card");
			ret.setMessage("没有红包");
		} else {
			ret.setBody(cardExtList);
		}

		return ret;
	}


	/**
	 * 店铺首页
	 * 一键领取红包集合
	 *
	 * @param sellerId
	 * @param actCardId
	 * @return
	 */
	@RequestMapping(value = "homeRedPackage/{sellerId}", method = RequestMethod.POST)
	@ResponseBody
	@Authentication
	public DataRet<List<ActUser>> getHomeRedPackage(@PathVariable("sellerId") Long sellerId, @RequestBody String actCardId) {
		DataRet<List<ActUser>> ret = new DataRet<List<ActUser>>();
		String[] actCardIds = actCardId.replace("[", "").replace("]", "").trim().split(",");
		List<String> actCardList = new ArrayList<>();
		for (String item : actCardIds) {
			if (!"".equals(item)) {
				actCardList.add(item);
			}
		}
		Long userId = WxSession.getUserId();
		if (null == userId) {
			ret.setErrorCode("no_login");
			ret.setMessage("没有登录");
		}
		if (null == actCardList||actCardList.size() == 0) {
			ret.setErrorCode("no_cart_id");
			ret.setMessage("红包ID不能为空");
		}
		List<ActUser> actUserList = sellerService.getRedPackageList(sellerId, userId, actCardList);
		if (actUserList == null||actUserList.size() <= 0) {
			ret.setErrorCode("no_card");
			ret.setMessage("没有红包");
			return ret;
		} else {
			ret.setBody(actUserList);
		}
		return ret;
	}

}
