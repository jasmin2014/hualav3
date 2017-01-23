package com.xyl.huala.wechat.v3.service.mysql;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xyl.core.jdbc.persistence.Criteria;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.entity.HSellerManage;
import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.entity.HUserExt;
import com.xyl.huala.entity.HUserExt.UserEnum;
import com.xyl.huala.entity.HUserFeedback;
import com.xyl.huala.entity.WxUser;
import com.xyl.huala.wechat.v3.dao.V3UserDao;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.V3UserService;
import com.xyl.huala.wechat.v3.util.BMapUtil;
import com.xyl.huala.weixin.wxapi.Qrcod;
import com.xyl.huala.weixin.wxapi.User;
import com.xyl.huala.weixin.wxapi.WxToken;

/**
 * 
 * @author leazx
 *
 */
@Service
@Profile("mysql")
public class V3UserServiceImpl implements V3UserService {
	@Autowired
	private V3UserDao userDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private JdbcDao jdbcDao;

	public List<HUserAddress> getAddressList(Long userId, String key) {

		String sql = "select * from h_user_address where user_id=? and is_delete='0'";
		if (key == null) {
			String sql2 = sql + " order by is_default desc";
			return jdbcDao.queryBySQL(sql2, HUserAddress.class, userId);
		} else {
			String sql2 = sql + " and address like '%" + key
					+ "%' order by is_default desc";
			return jdbcDao.queryBySQL(sql2, HUserAddress.class, userId);
		}
	}

	/**
	 * 获取地址信息，用户Id与地址ID不能为空
	 * 
	 * @param userId
	 *            用戶id
	 * @param addressId
	 *            地址id
	 * @param isDefault
	 *            是否默认，可以为空,如果是1则是默认
	 * @return
	 */
	public HUserAddress getAddress(Long userId, Long addressId, String isDefault) {
		return userDao.getAddress(userId, addressId, isDefault);
	}

	/**
	 * 删除地址
	 * 
	 * @param id
	 */
	public String deleteAddress(Long id) {
		HUserAddress h = jdbcDao.get(HUserAddress.class, id);
		if ("1".equals(h.getIsDefault())) {
			return "默认地址不能删除";
		}
		jdbcDao.delete(HUserAddress.class, id);
		return null;
	}

	/**
	 * 
	 * @param address
	 */
	@Transactional
	public HUserAddress updateAddress(HUserAddress address) {
		HUserAddress h = jdbcDao.get(HUserAddress.class, address.getId());
		jdbcDao.update(Criteria.create(HUserAddress.class)
				.set("is_default", "0").where("user_id", h.getUserId()));
		address.setIsDefault("1");
		jdbcDao.update(address);
		return address;
	}

	/**
	 * 查询用户的优惠信息
	 */
	public List<ActCardExt> getUserCoupon(Long userId) {
		return userDao.getUserCoupon(userId);
	}

	/**
	 * 取得收藏店铺信息
	 * 
	 * @param userId
	 * @return
	 */
	public List<Seller> getCollects(Long userId) {
		List<Seller> sellerList = userDao.getCollectsSeller(userId);
		return sellerList;
	}

	@Transactional
	public HUserAddress addNewAddress(HUserAddress userAddress) {
		Criteria criteria = Criteria.create(HUserAddress.class);
		criteria.set("is_default", "0");
		criteria.where("user_id", userAddress.getUserId());
		jdbcDao.update(criteria);
		userAddress.setIsDefault("1");
		Long id = jdbcDao.insert(userAddress);
		return jdbcDao.get(HUserAddress.class, id);
	}

	/**
	 * 收藏与取消店铺
	 * 
	 * @param type
	 *            类型 add 增加收藏，cancle 取消
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	@Transactional
	public int collect(Long userId, Long sellerId) {
		HUserExt h = new HUserExt(userId, UserEnum.店铺收藏, "" + sellerId);
		HUserExt hu = jdbcDao.queryOne(h);
		// 添加客户关系管理数据
		HSellerManage manage = new HSellerManage();
		manage.setSellerId(sellerId);
		manage.setUserId(userId);
		if (hu == null || !"1".equals(hu.getIsValid())) {
			h.setDataDesc("收藏商店的ID");
			h.setDataKey(sellerId + "");
			userDao.saveSsoLog(userId, sellerId, "add_collects", "收藏店铺："
					+ sellerId);
			// 将收藏记录保存至redis中 
			manage.setIsCollect("0");
			redisDao.getOperations().opsForList().leftPush("sellerManage", manage);
			if (hu == null) {
				return jdbcDao.insert(h).intValue();
			} else {
				return jdbcDao.update(Criteria.create(HUserExt.class)
						.set("is_valid", "1").where("id", hu.getId()));
			}
		} else {
			// 将收藏记录保存至redis中 
			manage.setIsCollect("1");
			redisDao.getOperations().opsForList().leftPush("sellerManage", manage);
			userDao.saveSsoLog(userId, sellerId, "cancel_collects", "取消收藏店铺："
					+ sellerId);
			return -jdbcDao.update(Criteria.create(HUserExt.class)
					.set("is_valid", "0").where("id", hu.getId()));
		}
	}

	/**
	 * 判断店铺是否已收藏
	 * 
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	public boolean iscollect(Long userId, Long sellerId) {
		HUserExt h = new HUserExt(userId, UserEnum.店铺收藏, "" + sellerId);
		h.setIsValid("1");
		HUserExt hu = jdbcDao.queryOne(h);
		if (hu == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 取得微信用户,如果没有关注, 获取微信二维码（带参数）
	 * 
	 * @param userId
	 */
	public String getQrcod(Long userId, Long sellerId) {
		HSsoUser user = jdbcDao.get(HSsoUser.class, userId);
		if (user != null && user.getOpenid() != null) {
			WxUser wu = new WxUser();
			wu.setOpenid(user.getOpenid());
			wu = jdbcDao.queryOne(wu);
			if (wu != null && wu.getSubscribe().equals("1")) {

			} else {
				Qrcod q = new Qrcod();
				String accToken = WxToken.getToken();
				JSONObject createLimitScene;
				String showqrcodeUrl = null;
				try {
					// 商铺ID作为场景ID
					createLimitScene = q.createScene(accToken, 24 * 3600,
							sellerId.toString());
					System.out.println(createLimitScene);
					showqrcodeUrl = Qrcod.showqrcodeUrl(createLimitScene
							.getString("ticket"));
					return showqrcodeUrl;
				} catch (InterruptedException | ExecutionException
						| IOException e) {
					// log.info(showqrcodeUrl);
					e.printStackTrace();
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * 插入用户反馈信息
	 * 
	 * @param userId
	 * @param content
	 * @return
	 */
	public Long saveFeedback(Long userId, String content) {
		HUserFeedback hUserFeedback = new HUserFeedback();
		hUserFeedback.setUserId(userId);
		hUserFeedback.setContent(content);
		hUserFeedback.setType("1");
		return jdbcDao.insert(hUserFeedback);
	}

	/**
	 * 获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public HSsoUser getUser(Long userId) {
		HSsoUser user = jdbcDao.get(HSsoUser.class, userId);
		return user;
	}

	/**
	 * 获取微信用户
	 * 
	 * @param openid
	 */
	public WxUser getWxUser(String openid) {
		WxUser entity = new WxUser();
		entity.setOpenid(openid);
		WxUser wxUser = jdbcDao.queryOne(entity);
		if (wxUser == null) {
			String token = WxToken.getToken();
			wxUser = User.getUserInfo(token, openid);
			jdbcDao.insert(wxUser);
		}
		return wxUser;
	}

	/**
     * 获取用户的省市区代码
     * @param lat 纬度
     * @param lng 经度
     */
	@Override
	public HUserAddress getProvince(String lng, String lat) {
		HUserAddress address = new HUserAddress();
		JSONObject jsonObj = this.getAddress(lat, lng);
		if(jsonObj != null) {
			String district = jsonObj.getString("adcode");
			if(StringUtils.isNotBlank(district)) {
				// 根据区号 查询省市区号
				HUserAddress addres = userDao.getProvince(district);
				if(addres != null) {
					address.setProvince(addres.getProvince());
					address.setCity(addres.getCity());
					address.setDistrict(district);
					return address;
				}
			}
		}
		// 否则默认是杭州的地址
		address.setProvince("330000");
		address.setCity("330100");
		address.setDistrict("330108");
		return address;
	}
	
	/**
     * 获取用户的省市区代码
     * @param lat 纬度
     * @param lng 经度
     */
	private JSONObject getAddress(String lat, String lng) {
		String json = BMapUtil.getAddressComponent(lat, lng);
		if(!StringUtils.isBlank(json)) {
			JSONObject jsonObj = JSON.parseObject(json);
			if("0".equals(jsonObj.getString("status"))) {
				// 查询成功之后解析数据
				jsonObj = JSON.parseObject(jsonObj.getString("result"));
				String address = jsonObj.getString("addressComponent");
				if(StringUtils.isNotBlank(address)) 
					return JSON.parseObject(address);
			}
		}
		return null;
	}
}
