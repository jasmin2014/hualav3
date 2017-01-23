package com.xyl.huala.sys.service.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.TUser;
import com.xyl.huala.sys.service.LoginService;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.xyl.pay.MD5;

@Service
@Profile("mysql")
public class LoginServiceImpl implements LoginService{
	@Autowired
	private JdbcDao jdbcDao;
	@Override
	public DataRet<String> login(String username, String password) {
		TUser u=new TUser();
		u.setAccount(username);
		u.setPassword(MD5.getCode(password));
		u=jdbcDao.queryOne(u);
		if(u!=null){
			DataRet<String> ret= new DataRet<String>("登录成功");
			ret.setToken(MD5.md5Encrypt(username, password));
			return ret;
		}else{
			return new DataRet<String>("login err","用户名或密码错误");
		}
		
	}

}
