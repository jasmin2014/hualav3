package com.xyl.huala.wechat.v3.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import com.xyl.huala.utils.EmojiFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;
import com.xyl.huala.domain.Gps;
import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.entity.WxUser;
import com.xyl.huala.wechat.v3.config.Authentication;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.V3UserService;
import com.xyl.huala.wechat.v3.util.BMapUtil;
import com.xyl.huala.wechat.v3.util.SearchResult;
import com.xyl.huala.wechat.v3.util.WxSession;

/**
 * 购物车信息
 *
 * @author wyl0153
 */
@RestController
@RequestMapping("v3")
public class V3UserWeb {
	private static final Logger logger = LoggerFactory.getLogger(V3UserWeb.class);

	@Autowired
	private V3UserService userService;

	/**
	 * 获取地址列表
	 *
	 * @param userId
	 * @param model
	 * @return
	 */
	@RequestMapping("center")
	@Authentication
	public DataRet<Map<String, String>> center() {
		Map<String, String> map = new HashMap<String, String>();
		DataRet<Map<String, String>> ret = new DataRet<Map<String, String>>();
		Long userId = WxSession.getUserId();
		HSsoUser user = userService.getUser(userId);
		if (user.getOpenid() != null) {
			WxUser wxuser = userService.getWxUser(user.getOpenid());
			map.put("name", wxuser.getNickname());
			map.put("picUrl", wxuser.getHeadimgurl());//头像地址
		}
		map.put("mobile", user.getMobile());
		map.put("customPhone", System.getProperty("server_cfg.custom_phone"));
		map.put("serverTime", System.getProperty("server_cfg.openTime") + "-" + System.getProperty("server_cfg.closeTime"));
		ret.setBody(map);
		return ret;
	}

	/**
	 * 获取地址列表
	 *
	 * @param userId
	 * @param model
	 * @return
	 */
	@RequestMapping("address/list")
	@Authentication
	public DataRet<List<HUserAddress>> getAddressList(String key) {
		DataRet<List<HUserAddress>> ret = new DataRet<List<HUserAddress>>();
		Long userId = WxSession.getUserId();
		List<HUserAddress> uaList = userService.getAddressList(userId, key);
		ret.setBody(uaList);
		return ret;
	}

	/**
	 * 获取用户默认地址
	 *
	 * @return
	 */
	@RequestMapping("address/default")
	public DataRet<HUserAddress> getDefAddress() {
		DataRet<HUserAddress> ret = new DataRet<HUserAddress>();
		ret.setBody(userService.getAddress(WxSession.getUserId(), null, "1"));
		return ret;
	}

	/**
	 * 获取用户默认地址
	 *
	 * @return
	 */
	@RequestMapping("address/get/{id}")
	@Authentication
	public DataRet<HUserAddress> getAddress(@PathVariable Long id) {
		DataRet<HUserAddress> ret = new DataRet<HUserAddress>();
		ret.setBody(userService.getAddress(WxSession.getUserId(), id, null));
		return ret;
	}

	/**
	 * 删除地址
	 *
	 * @return
	 */
	@RequestMapping("address/del/{id}")
	@Authentication
	public DataRet<String> deleteAddress(@PathVariable Long id) {
		DataRet<String> ret = new DataRet<String>();
		String message = userService.deleteAddress(id);
		if (message != null) {
			ret.setErrorCode("error");
			ret.setMessage(message);
		}
		return ret;
	}

	/**
	 * 编辑地址
	 *
	 * @param addressid
	 * @param consignee
	 * @param mobile
	 * @param addressName
	 * @param address
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "address/update/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authentication
	public DataRet<String> addressEdit(@PathVariable Long id,
									   @RequestBody @Valid HUserAddress address, BindingResult result) {
		DataRet<String> ret = new DataRet<String>();
		if (result.hasErrors()) {
			ret.setMessage(JSON.toJSONString(result.getAllErrors()));
			return ret;
		}
		// 根据经纬度  获取省市区编号
		HUserAddress province = userService.getProvince(address.getLng() + "", address.getLat() + "");
		address.setDistrict(province.getDistrict());
		address.setCity(province.getCity());
		address.setProvince(province.getProvince());
		userService.updateAddress(address);
		return ret;
	}

	/**
	 * 编辑新增
	 *
	 * @param addressid
	 * @param consignee
	 * @param mobile
	 * @param addressName
	 * @param address
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "address/add/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Authentication
	public DataRet<HUserAddress> addressAdd(@PathVariable Long id,
											@RequestBody @Valid HUserAddress address, BindingResult result) {
		DataRet<HUserAddress> ret = new DataRet<HUserAddress>();
		if (result.hasErrors()) {
			ret.setMessage(JSON.toJSONString(result.getAllErrors()));
			return ret;
		}
		Long userId = WxSession.getUserId();
		address.setUserId(userId);
		// 根据经纬度  获取省市区编号
		HUserAddress province = userService.getProvince(address.getLng() + "", address.getLat() + "");
		address.setDistrict(province.getDistrict());
		address.setCity(province.getCity());
		address.setProvince(province.getProvince());
		HUserAddress newAddress = userService.addNewAddress(address);
		ret.setBody(newAddress);
		return ret;
	}

	/**
	 * 地址模糊查询
	 *
	 * @param kw
	 * @param gps
	 * @return
	 */
	@RequestMapping("address/search")
	public DataRet<List<SearchResult>> searchAddress(@CookieValue("currentCity") String city, String kw, Gps gps) throws Exception {
		city = StringUtils.isBlank(city) ? "杭州" : city.replace("\"", "");
		DataRet<List<SearchResult>> ret = new DataRet<List<SearchResult>>();
		Double lat = gps.getLat() == null ? new Double("0") : gps.getLat();
		Double lng = gps.getLng() == null ? new Double("0") : gps.getLng();
		if (StringUtils.isBlank(kw)) {
			List<SearchResult> circle = BMapUtil.circlePlaceSearch(lat, lng);
			ret.setBody(circle);
		} else {
			ret.setBody(BMapUtil.getPlaceSuggestion(city, kw, lat, lng));
		}
		return ret;
	}

	/**
	 * 获取用户的优惠卡券信息
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "coupon/view", method = RequestMethod.GET)
	@Authentication
	public DataRet<List<ActCardExt>> getCouponInfo() {
		DataRet<List<ActCardExt>> ret = new DataRet<List<ActCardExt>>();
		Long userId = WxSession.getUserId();
		List<ActCardExt> list = userService.getUserCoupon(userId);
		ret.setBody(list);
		return ret;
	}

	/**
	 * 获取收藏店铺信息
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "collects", method = RequestMethod.GET)
	@Authentication
	public DataRet<List<Seller>> getCollects(Model model) {
		DataRet<List<Seller>> ret = new DataRet<List<Seller>>();
		Long userId = WxSession.getUserId();
		List<Seller> sellerList = userService.getCollects(userId);
		ret.setBody(sellerList);
		return ret;
	}

	/**
	 * 店铺收藏接口
	 *
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "collects/{sellerId}", method = RequestMethod.POST)
	@Authentication
	public DataRet<String> getCollects(@PathVariable("sellerId") Long sellerId) {
		DataRet<String> ret = new DataRet<String>();
		Long userId = WxSession.getUserId();
		int i = userService.collect(userId, sellerId);
		if (i == 0) {
			ret.setErrorCode("fail");
			ret.setMessage("店铺收藏处理失败");
		} else {
			if (i > 0) {
				ret.setMessage(userService.getQrcod(userId, sellerId));
			}
			ret.setBody("" + i);
		}
		//logger.info(JSONObject.toJSONString(ret));
		return ret;
	}

	/**
	 * 店铺是否已收藏
	 */
	@RequestMapping(value = "iscollects/{sellerId}", method = RequestMethod.GET)
	public DataRet<String> isCollects(@PathVariable("sellerId") Long sellerId) {
		DataRet<String> ret = new DataRet<String>();
		Long userId = WxSession.getUserId();
		if (userId == null) {
			ret.setErrorCode("fail");
			return ret;
		}
		boolean b = userService.iscollect(userId, sellerId);
		if (!b) {
			ret.setErrorCode("fail");
			ret.setMessage("店铺没有收藏");
		}
		return ret;
	}

	/**
	 * 保存用户反馈信息
	 *
	 * @param hUserFeedback
	 * @param backUrl
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value = "feedback", method = RequestMethod.POST)
	@Authentication
	public DataRet<String> saveUserFeedback(String content,
											BindingResult bindingResult, Model model) {
		DataRet<String> ret = new DataRet<String>();
		if (StringUtils.isBlank(content)) {
			ret.setErrorCode("err feedback");
			ret.setMessage("反馈内容不能为空");
			return ret;
		}
		Long userId = WxSession.getUserId();
		userService.saveFeedback(userId, EmojiFilter.filterEmoji(content));
		return ret;
	}
}
