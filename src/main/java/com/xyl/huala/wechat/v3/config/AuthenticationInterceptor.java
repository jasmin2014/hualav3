package com.xyl.huala.wechat.v3.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.util.WxSession;

/**
 * 自动登录拦截器
 * 
 * @author zxl0047
 *
 */
@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {
	private static final Logger logger = LoggerFactory
			.getLogger(AuthenticationInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res,
			Object handler) throws Exception {
		res.setHeader("Access-Control-Allow-Origin", "*");
		res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		res.setHeader("Access-Control-Max-Age", "3600");
		res.addHeader("Access-Control-Allow-Headers", "X-Requested-With, Cache-Control,  Content-Type");
		
		if(WxSession.getUserId()==null){
			clearCookieUserId(req,res);
		}
		if (handler instanceof HandlerMethod) {
			HandlerMethod handle = (HandlerMethod) handler;
			Authentication authentication=handle.getMethodAnnotation(Authentication.class);
			if(authentication!=null){
				Subject subject = SecurityUtils.getSubject();
				if(!subject.isAuthenticated()){
					res.setStatus(HttpStatus.UNAUTHORIZED.value());
					DataRet<String> ret=new DataRet<>();
					ret.setErrorCode("no Authentication");
					ret.setMessage("没有登录");
					res.getWriter().write(JSON.toJSONString(ret));
					return false;
				}
			}
			
		}
		return super.preHandle(req, res, handler);
	}

	/**
	 * 如果没登录清空cookie中的用户的userId
	 */
	private void clearCookieUserId(HttpServletRequest req, HttpServletResponse res){
		Cookie[] cookies=req.getCookies();
		if(cookies==null){
			return ;
		}
		for(Cookie c:cookies){
			if("USERID".equals(c.getName())){
				c.setPath("/");
				c.setMaxAge(0);
				c.setValue(null);
				res.addCookie(c);
				System.out.println(c);
			}
		}
	}
	
	
}
