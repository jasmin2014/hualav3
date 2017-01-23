package com.xyl.huala.wechat.v3.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xyl.huala.utils.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyl.huala.core.annotation.LoginUser;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.wechat.v3.domain.ActCardExt;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.V3ScanCodeService;
import com.xyl.huala.wechat.v3.service.V3SellerService;
import com.xyl.huala.wechat.v3.util.BalanceUtils;
import com.xyl.huala.wechat.v3.util.WxSession;
import com.xyl.huala.weixin.util.Pic;
import com.xyl.huala.weixin.util.WxUtils;

/**
 * 微信或支付宝扫码直付
 *
 * @author leazx
 */
@Controller
@RequestMapping("v3")
public class V3ScanCodeWeb {
	private static final Logger logger = Logger.getLogger(V3ScanCodeWeb.class);
	@Autowired
	private V3ScanCodeService scanCodeService;
	@Autowired
	private V3SellerService sellerService;
	/************************************************************************
	 * 支付宝或微信扫码直付    scanPay()方法是用户扫码之后调用的方法    scanGoPay()确认支付
	 ************************************************************************/
	/**
	 * 跳转到二维码的页面
	 */
	@RequestMapping(value = "/go_scan_code/{sellerId}", method = RequestMethod.GET)
	public String goScanCode(@PathVariable Long sellerId, HttpServletRequest request, Model model) {
		StringBuffer requestURL = request.getRequestURL();
		int contextIndex = requestURL.indexOf(request.getContextPath());
		String host = requestURL.substring(0, contextIndex + request.getContextPath().length());
		String content = host + "/v3/scan_code/" + sellerId;
		if(this.isNewScan(sellerId)) {
			content = System.getProperty("scan_pay.new_scan_url") + "scan.html?sellerId=" + sellerId;
		}
		// 测试用
		String logoUrl = host + "/assets/v3/images/huala-logo.jpg";
		// 生成微信的支付二维码
		BufferedImage wxImg = scanCodeService.genQrcode(sellerId, content, logoUrl);
		// 生成支付宝的二维码
		String url = this.getAliOauth2(content);
		logger.info("=支付宝授权地址=" + url);
		BufferedImage aliImg = scanCodeService.genQrcode(sellerId, url, logoUrl);
		model.addAttribute("wxCode", Pic.base64Encode(wxImg));
		model.addAttribute("aliCode", Pic.base64Encode(aliImg));
		return "v3/pay/code";
	}

	/**
	 * 判断是否使用新的扫码支付
	 */
	private boolean isNewScan(Long sellerId) {
		// 0或空代表使用老接口 1或有店铺id字符串代表使用新的接口
		String text = System.getProperty("scan_pay.scan_new_seller");
		if("1".equals(text)) {
			return true;
		}
		if("0".equals(text)) {
			return false;
		}
		if(StringUtils.isNotBlank(text)) {
			String[] sellerIds = text.split(",");
			return Arrays.asList(sellerIds).contains(String.valueOf(sellerId));
		}
		return false;
	}

	/**
	 * 支付宝授权地址
	 *
	 * @param url
	 */
	private String getAliOauth2(String url) {
		String appId = System.getProperty("ali_fuwuchuang.app_id");
		return "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id="
			+ appId + "&scope=auth_base&redirect_uri=" + url;
	}

	/**
	 * 生成二维码的图片 默认是微信的二维码
	 */
	@RequestMapping(value = "/gen_qrcode/{sellerId}", method = RequestMethod.GET)
	public void genQrcode(@PathVariable Long sellerId,
						  HttpServletResponse response) throws IOException {
		String host = System.getProperty("server_cfg.userUrl");
		String content = host + "v3/scan_code/" + sellerId;
		if(this.isNewScan(sellerId)) {
			content = System.getProperty("scan_pay.new_scan_url") + "scan.html?sellerId=" + sellerId;
		}
		String logoUrl = host + "/assets/v3/images/huala-logo.jpg";
		BufferedImage img = scanCodeService.genQrcode(sellerId, content, logoUrl);
		ImageIO.write(img, "PNG", response.getOutputStream());
	}

	/**
	 * 用户扫码的接口 返回商家和活动信息
	 */
	@RequestMapping(value = "/scan_code/{sellerId}", method = RequestMethod.GET)
	public String scanCode(@PathVariable Long sellerId) {
		if(this.isNewScan(sellerId)){
			return "redirect:"+System.getProperty("scan_pay.new_scan_url") + "scan.html?sellerId=" + sellerId;
		}
		return "redirect:/v3#/scan_pay/" + sellerId;
	}

	/**
	 * 用户扫码的接口 返回商家和活动信息
	 */
	@RequestMapping(value = "/scan_pay/{sellerId}", method = RequestMethod.GET)
	@ResponseBody
	@LoginUser(redirect = false)
	public DataRet<Map<String, Object>> scanPay(@PathVariable Long sellerId,
												HttpServletRequest request) {
		DataRet<Map<String, Object>> ret = new DataRet<Map<String, Object>>();
		Map<String, Object> resMap = new HashMap<String, Object>(3);
		Long userId = WxSession.getUserId();
		Seller seller = scanCodeService.getSellerActInfoById(sellerId);
		resMap.put("seller", seller);
		ActCardExt card = scanCodeService.getRedPackage(sellerId, userId);
		String content = "暂无活动";
		if (card != null) {
			content = "满" + NumberUtils.toYuan(String.valueOf(card.getSums()))
				+ " 减" + NumberUtils.toYuan(String.valueOf(card.getBalance()));
			resMap.put("card", card);
		}
		resMap.put("content", content);
		ret.setBody(resMap);
		return ret;
	}

	/**
	 * 确认支付
	 *
	 * @param sellerId  店铺id
	 * @param payAmount 用户的支付金额
	 * @param amount    用户的输入金额
	 * @param actCardId
	 * @param actUserId
	 */
	@RequestMapping(value = "/scan_go_pay/{sellerId}", method = RequestMethod.POST)
	@ResponseBody
	public DataRet<String> scanGoPay(@PathVariable Long sellerId, String payAmount,
									 String amount, String actCardId, String actUserId,
									 String isWxScan, HttpServletRequest request) {
		String openId = WxSession.getOpenId();
		String aliPayId = WxSession.getAliUserId();
		Long userId = WxSession.getUserId();
		DataRet<String> ret = new DataRet<String>();
		Seller seller = scanCodeService.getSellerActInfoById(sellerId);
		if ("1".equals(seller.getSellerStatus())) {
			ret.setErrorCode("seller_is_close");
			ret.setMessage("店铺未营业或已关闭,不能扫码下单！");
			return ret;
		}
		if(!"0".equals(seller.getIsDelete())) {
			ret.setErrorCode("seller_is_close");
			ret.setMessage("店铺未营业或已关闭,不能扫码下单！");
			return ret;
		}
		if (!WxUtils.isAliBrower(request) && !WxUtils.isWeiXin(request)) {
			ret.setErrorCode("pay_error");
			ret.setMessage("请使用手机支付!");
			return ret;
		}

		return scanCodeService.addOrderInfo(sellerId, payAmount, amount,
			actCardId, actUserId, userId, isWxScan,openId,aliPayId);
	}

	/**
	 * 支付成功之后跳转到成功的页面
	 */
	@RequestMapping(value = "/scan_pay_success/{orderId}", method = RequestMethod.GET)
	@ResponseBody
	public DataRet<Map<String, Object>> scanPaySuccess(@PathVariable Long orderId) {
		DataRet<Map<String, Object>> ret = new DataRet<Map<String, Object>>();
		Map<String, Object> resMap = new HashMap<String, Object>(3);
		HOrder order = scanCodeService.getOrderInfoById(orderId);
		resMap.put("order", order);
		resMap.put("sellerName", scanCodeService.getSellerNameById(Long.parseLong(order.getReferer())));
		ret.setBody(resMap);
		return ret;
	}
}
