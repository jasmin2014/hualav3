package com.xyl.huala.wechat.v3.service.mongo;

import java.util.Date;
import java.util.regex.Pattern;

import com.xyl.huala.utils.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.wechat.v3.dao.DataSyncDao;
import com.xyl.huala.wechat.v3.dao.V3GoodsSellerDao;
import com.xyl.huala.wechat.v3.domain.CartInfo;

@Service
public class TaskService {
	private static Logger log = Logger.getLogger(TaskService.class);
	@Autowired
	private JdbcDao jdbcDao;
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private MongoTemplate mongo;
	@Autowired
	private V3GoodsSellerDao goodsSellerDao;
	@Autowired
	private DataSyncDao dataSyncDao;
	
	/**
	 * 删除会话中24小后过期的购物车
	 */
	@Scheduled(fixedDelay=3600*1000)
	public void deleteCartInfo(){
		Pattern p=Pattern.compile("[-]+");
		mongo.remove(Query.query(Criteria.where("userId").regex(p).and("addTime").lt(DateUtils.addMinutes(new Date(), -60*6))), CartInfo.class);
		log.info("清理过期会话中购物车");
	}
	
}

