package com.xyl.huala.wechat.v3.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.xyl.huala.entity.HSsoUser;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.entity.WxUser;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.Seller;

/**
 * 
 * @author leazx
 *
 */
public interface V3UserService {

	/**
	 * 获取地址信息
	 * @param userId
	 * @param key
	 * @return
	 */
	public List<HUserAddress> getAddressList(Long userId, String key);

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
	public HUserAddress getAddress(Long userId, Long addressId, String isDefault);

	/**
	 * 删除地址
	 * 
	 * @param id
	 */
	public String deleteAddress(Long id);

	/**
	 * 
	 * @param address
	 */
	@Transactional
	public HUserAddress updateAddress(HUserAddress address);

	/**
	 * 查询用户的优惠信息
	 */
	public List<ActCardExt> getUserCoupon(Long userId);

	/**
	 * 取得收藏店铺信息
	 * 
	 * @param userId
	 * @return
	 */
	public List<Seller> getCollects(Long userId);

	@Transactional
	public HUserAddress addNewAddress(HUserAddress userAddress);

	/**
	 * 收藏与取消店铺
	 * 
	 * @param type
	 *            类型 add 增加收藏，cancle 取消
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	@Transactional
	public int collect(Long userId, Long sellerId);

	/**
	 * 判断店铺是否已收藏
	 * 
	 * @param userId
	 * @param sellerId
	 * @return
	 */
	public boolean iscollect(Long userId, Long sellerId);

	/**
	 * 取得微信用户,如果没有关注, 获取微信二维码（带参数）
	 * 
	 * @param userId
	 */
	public String getQrcod(Long userId, Long sellerId);

	/**
	 * 插入用户反馈信息
	 * 
	 * @param userId
	 * @param content
	 * @return
	 */
	public Long saveFeedback(Long userId, String content);

	/**
	 * 获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public HSsoUser getUser(Long userId);

	/**
	 * 获取微信用户
	 * 
	 * @param openid
	 */
	public WxUser getWxUser(String openid);

	/**
	 * 根据用户经纬度获取用户的省市区地址
	 */
	public HUserAddress getProvince(String lng, String lat);
}
