package com.xyl.huala.sys.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.xyl.huala.sys.service.LoginService;
import com.xyl.huala.wechat.v3.config.Authentication;
import com.xyl.huala.wechat.v3.domain.DataRet;

@RestController
public class SysLoginWeb {
	private static final Logger logger = Logger.getLogger(SysLoginWeb.class);

	@Autowired
	private LoginService loginService;
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@RequestMapping(value="sys/login",method=RequestMethod.POST)
	public DataRet<String> login(@RequestBody JSONObject json){
		return loginService.login(json.getString("username"), json.getString("password"));
	}

}
