
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.xyl.core.utils.HttpKit;
import com.xyl.huala.xyl.credit.CreditInfo;
import com.xyl.huala.xyl.credit.CreditResponse;

public class RedisTest {

	@Test
	public void redis1() {
		
		Map<String, String> params;
		String url = "http://192.168.79.207:8080/web/external/jinfu/creditInfo";
		params=new HashMap<>();
		params.put("guid", "1111111111161");
		String aaa=HttpKit.get(url, params);
		CreditResponse a=JSONObject.parseObject(aaa,CreditResponse.class);
		CreditInfo b=JSONObject.parseObject(a.getData().toString(),CreditInfo.class);
		System.out.println(a);
	}
}
