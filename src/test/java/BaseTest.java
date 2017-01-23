

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.druid.pool.DruidDataSource;
import com.mongodb.Mongo;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.jdbc.persistence.JdbcDaoImpl;
import com.xyl.core.redis.RedisDao;
import com.xyl.core.redis.RedisDaoImpl;
import com.xyl.huala.wechat.v3.config.MongoConfig;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BaseTest.class,MongoConfig.class})
@ComponentScan(basePackages="com.xyl.**.v3")
@Configuration
public class BaseTest {
	private static final Logger logger = Logger.getLogger(BaseTest.class);
	@Autowired
	private Environment environment;
	@Bean
	public DataSource dataSource() throws Exception {

		DruidDataSource basicDataSource = new DruidDataSource();
		basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		basicDataSource.setPassword("xpsh");
		basicDataSource.setUrl("jdbc:mysql://192.168.200.152/huala_test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
		basicDataSource.setUsername("root");
		basicDataSource.setInitialSize(5);
		basicDataSource.setMaxActive(10);
		basicDataSource.setFilters("stat");
		logger.info("使用本地数据源：" );
		return basicDataSource;
	}
	/**
	 * 事务管理
	 * 
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean
	public DataSourceTransactionManager transactionManager() throws Exception {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(this.dataSource());
		return transactionManager;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() throws Exception {
		JdbcTemplate jdbcTemplate2 = new JdbcTemplate(this.dataSource());
		return jdbcTemplate2;
	}
	@Bean
	public JdbcDao jdbcDao(){
		return new JdbcDaoImpl();
	}
	
	/**
	 * redis缓存池配置
	 * 
	 * @return
	 */
	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig jp = new JedisPoolConfig();
		jp.setMaxIdle(300);
		jp.setMaxTotal(600);
		jp.setMaxWaitMillis(1000);
		jp.setTestOnBorrow(false);
		jp.setTestWhileIdle(false);
		return jp;
	}

	@Bean(name="jedisFactory")
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory jf = new JedisConnectionFactory();
		jf.setPoolConfig(jedisPoolConfig());
		jf.setDatabase(10);
		jf.setHostName("192.168.200.153");
		jf.setPort(6379);
		return jf;
	}

	@SuppressWarnings("rawtypes")
	@Bean
	public RedisTemplate redisTemplate() {
		RedisTemplate<Object, Object> rt = new RedisTemplate<Object, Object>();
		rt.setConnectionFactory(jedisConnectionFactory());
		rt.setDefaultSerializer(new StringRedisSerializer());
		rt.setKeySerializer(new StringRedisSerializer());
		rt.setValueSerializer(new JdkSerializationRedisSerializer());
		return rt;
	}
	
	@Bean
	public RedisDao redisDao(){
		return new RedisDaoImpl(this.redisTemplate());
	}
	
}
