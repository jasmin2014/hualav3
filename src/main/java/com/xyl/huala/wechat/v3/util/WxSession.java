package com.xyl.huala.wechat.v3.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyl.huala.entity.HSsoUser;

/**
 * 用户帐号绑定帮助类
 * 
 * @author zxl
 * @version $Id: UserHelper.java
 */
public class WxSession {
	private static final Logger logger = LoggerFactory
			.getLogger(WxSession.class);



	/**
	 * 获取登录上下文.
	 * 
	 * @return
	 */
	public static Session getSession() {
		return SecurityUtils.getSubject().getSession();
	}

	/**
	 * 获取登录上下文.
	 * 
	 * @return
	 */
	public static Long getUserId() {
		HSsoUser subject = (HSsoUser)SecurityUtils.getSubject().getPrincipal();
		if(subject==null){
			return null;
		}
		return subject.getId();
	}
	/**
	 * 设置客户端类型（微信端：wechat，苹果端：ios，安卓端：android）
	 * 
	 * @return
	 */
	public static void setClientType(ClientType iso) {
		Session session = getSession();
		session.setAttribute("clientType", iso);
	}
	/**
	 * 获取客户端类型（微信端：wechat，苹果端：ios，安卓端：android）
	 * 
	 * @return
	 */
	public static ClientType getClientType() {
		Session session = getSession();
		ClientType clientType = (ClientType)session.getAttribute("clientType");
		return clientType;
	}
	/**
	 * 获取微信openId(微信用户)
	 * 
	 * @return
	 */
	public static String getOpenId() {
		Session session = getSession();
		return (String) session.getAttribute("openid");
	}

	/**
	 * 获取微信openId(微信用户)
	 * 
	 * @return
	 */
	public static void setOpenId(String openId) {
		Session session = getSession();
		session.setAttribute("openid", openId);
	}

	/**
	 * 客户端类型
	 * @author zxl0047
	 *
	 */
	public enum ClientType{
		IOS("ios"),ANDROID("android"),WECHAT("wechat");
		private String type;
		private ClientType(String type) {
			this.type=type;
		}
		@Override
		public String toString() {
			return type;
		}
		
	}
	
	/**
	 * 获取alipay的userId
	 * 
	 * @return
	 */
	public static void setAliUserId(String aliUserId) {
		Session session = getSession();
		session.setAttribute("aliUserId", aliUserId);
	}
	/**
	 * 获取alipay的userId
	 * 
	 * @return
	 */
	public static String getAliUserId() {
		Session session = getSession();
		return (String) session.getAttribute("aliUserId");
	}
}
