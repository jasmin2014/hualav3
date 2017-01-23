package com.xyl.huala.sys.service;

import com.xyl.huala.entity.TUser;
import com.xyl.huala.wechat.v3.domain.DataRet;

public interface LoginService {

	/**
	 * 用户登录
	 * @param username
	 * @param password
	 * @return
	 */
	public DataRet<String> login(String username,String password);
}
