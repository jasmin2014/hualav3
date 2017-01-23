package com.xyl.huala.wechat.v3.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * v3版本首页相关接口
 * @author leazx
 *
 */
public interface V3IndexService {

	/**
	 * 生成维信场景二维码图片
	 * @param sellerId
	 * @param request
	 * @return
	 */
	public BufferedImage getWxQcord(String sellerId,String url);

	BufferedImage getSellerWxAddressQcord(String sellerId,String Url) throws IOException;
}
