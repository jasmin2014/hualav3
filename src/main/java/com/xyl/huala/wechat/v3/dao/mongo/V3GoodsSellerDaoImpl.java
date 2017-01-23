package com.xyl.huala.wechat.v3.dao.mongo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.wechat.v3.dao.V3GoodsSellerDao;

/**
 * @tag
 */
@Repository
@Profile("mongo")
@SuppressWarnings({ })
public class V3GoodsSellerDaoImpl implements V3GoodsSellerDao{
	private static final Logger logger = Logger.getLogger(V3GoodsSellerDaoImpl.class);
	@Autowired
	private JdbcDao jdbcDao;
	
}
