/** 
 * 
 */
package com.xyl.huala.wechat.v3.dao;

import java.util.List;
import org.springframework.scheduling.annotation.Async;
import com.xyl.huala.entity.ActUser;
import com.xyl.huala.entity.HUserAddress;
import com.xyl.huala.enums.SellerEnum;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.Seller;

/**   
 * @tag
 */
public interface V3UserDao{


	/**
	 * 获取扩展属性值
	 * @param sellerEnum
	 * @return
	 */
	public String getExt(Long sellerId,SellerEnum sellerEnum);



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
	 * 得到优惠券信息
	 */
	public List<ActCardExt> getUserCoupon(Long userId);

	/**
	 * 取得用户收藏店铺列表
	 * @param userId
	 * @return
	 */
	public List<Seller> getCollectsSeller(Long userId);
	
	/**
	 * 查询用户下的订单数
	 * @param district
	 * @return 
	 */
	public int getUserOrderCount(Long uid);
	
	/**
	 * 保存用户操作日志
	 */
	@Async
	public void saveSsoLog(Long userId,Long sellerId,String type,String remark);
	
	/**
	 * 根据区号 查询省市编号
	 */
	public HUserAddress getProvince(String district);
	
	/**
	 * 获取用户今日是否已经领取了每日红包
	 */
	public List<ActUser> getRedPackage(Long userId);
}
