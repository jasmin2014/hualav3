

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSON;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.V3SellerService;
import com.xyl.huala.wechat.v3.service.mongo.DataSyncService;

public class MongodbTest extends BaseTest{
	private static final Logger log = Logger.getLogger(MongodbTest.class);
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private MongoTemplate mongo;

	@Autowired
	private V3SellerService v3IndexService;
	@Autowired
	private DataSyncService dataSyncService;
	

	
	//@Test
	public void getSeller(){
		DataRet<List<GeoResult<Seller>>> ret = new DataRet<List<GeoResult<Seller>>>();
		Point gps = new Point(120.20860113943, 30.213323964311);
		// 查询出附近的商家
		List<GeoResult<Seller>>  list = v3IndexService.getIndexSeller(gps, 10,new PageRequest(0, 100));

		if(list.iterator().hasNext()){
			ret.setErrorCode("no result");
		}
		ret.setBody(list);
	}

}
