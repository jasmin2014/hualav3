package com.xyl.huala.wechat.v3.dao.mysql;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.HSellerGoodsExtends;
import com.xyl.huala.enums.SellerGoodsEnum;
import com.xyl.huala.wechat.v3.dao.V3GoodsSellerDao;
import com.xyl.huala.wechat.v3.domain.GoodsAct;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.domain.SellerGoodsCat;
import com.xyl.huala.wechat.v3.util.PropUtil;

/**
 * @tag
 */
@Repository
@Profile("mysql")
@SuppressWarnings({ })
public class V3GoodsSellerDaoImpl implements V3GoodsSellerDao{
	private static final Logger logger = Logger.getLogger(V3GoodsSellerDaoImpl.class);
	@Autowired
	private JdbcDao jdbcDao;
	
}
