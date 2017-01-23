/**
 * 
 */
package com.xyl.huala.wechat.v3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.xyl.core.jdbc.persistence.JdbcDao;

/**
 * @author leazx
 *
 */
@Configuration
public class MongoConfig extends AbstractMongoConfiguration {
	@Autowired
	private JdbcDao jdbcDao;

	@Override
	protected String getDatabaseName() {
		return "database";
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(System.getProperty("mongo.host"));
		//return new MongoClient("192.168.200.152");
	}

//	@Bean
//	public MessageListenerAdapter messageListener() {
//		MessageListenerAdapter m = new MessageListenerAdapter();
//		m.setDelegate(new MessageDelegateListenerImpl());
//		return m;
//	}
//
//	@Bean
//	public RedisMessageListenerContainer container(RedisTemplate redis) {
//		RedisMessageListenerContainer c = new RedisMessageListenerContainer();
//		c.addMessageListener(messageListener(), new ChannelTopic("java"));
//		c.setConnectionFactory(redis.getConnectionFactory());
//		return c;
//	}
}
