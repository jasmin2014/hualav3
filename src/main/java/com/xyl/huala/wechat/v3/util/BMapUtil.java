package com.xyl.huala.wechat.v3.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xyl.core.utils.HttpKit;
import com.xyl.huala.domain.Gps;

public class BMapUtil {

	private static Logger logger = Logger.getLogger(BMapUtil.class);
	private static String baiduMapKey = "16005cc9538518f22a3b1c48fe36ea96";

	/**
	 * 百度API,圆形区域检索
	 */
	private static final String CIRCLE_PLACE_RUI = "http://api.map.baidu.com/place/v2/search";
	/**
	 * 百度API,匹配用户输入关键字辅助信息、提示
	 */
	private static final String PLACE_SUGGESTION_URI = "http://api.map.baidu.com/place/v2/suggestion";
	/**                                                     
	 * 坐标转换API
	 */
	private static final String PLACE_GEOCONV_URI = "http://api.map.baidu.com/geoconv/v1/";
	/**
	 * 地址解析，由详细到街道的结构化地址得到百度经纬度信息
	 */
	private static final String GEOCODING_URI = "http://api.map.baidu.com/geocoder/v2/";
	
	/**
	 * 百度API,匹配用户输入关键字辅助信息、提示
	 */
	private static final String PLACE_DETAIL = "http://api.map.baidu.com/place/v2/detail";
	
	

	/**
	 * 匹配用户输入关键字辅助信息、提示，同时传入经纬度后返回结果将以距离进行排序
	 * 
	 * @param query
	 *            输入建议关键字（支持拼音）
	 * @param region
	 *            所属城市/区域名称或代号
	 * @param lat
	 *            纬度
	 * @param lng
	 *            经度
	 * @return <pre class="de1">
	 * <span class="br0">{</span>
	 *     <span class="st0">"status"</span><span class="sy0">:</span><span class="nu0">0</span><span class="sy0">,</span>
	 *     <span class="st0">"message"</span><span class="sy0">:</span><span class="st0">"ok"</span><span class="sy0">,</span>
	 *     <span class="st0">"result"</span><span class="sy0">:</span><span class="br0">[</span>
	 *         <span class="br0">{</span>
	 *             <span class="st0">"name"</span><span class="sy0">:</span><span class="st0">"天安门"</span><span class="sy0">,</span>
	 *             <span class="st0">"location"</span><span class="sy0">:</span><span class="br0">{</span>
	 *                 <span class="st0">"lat"</span><span class="sy0">:</span><span class="nu0">39.915174</span><span class="sy0">,</span>
	 *                 <span class="st0">"lng"</span><span class="sy0">:</span><span class="nu0">116.403875</span>
	 *             <span class="br0">}</span><span class="sy0">,</span>
	 *             <span class="st0">"uid"</span><span class="sy0">:</span><span class="st0">"65e1ee886c885190f60e77ff"</span><span class="sy0">,</span>
	 *             <span class="st0">"city"</span><span class="sy0">:</span><span class="st0">"北京市"</span><span class="sy0">,</span>
	 *             <span class="st0">"district"</span><span class="sy0">:</span><span class="st0">"东城区"</span><span class="sy0">,</span>
	 *             <span class="st0">"business"</span><span class="sy0">:</span><span class="st0">""</span><span class="sy0">,</span>
	 *             <span class="st0">"cityid"</span><span class="sy0">:</span><span class="st0">"131"</span>
	 *         <span class="br0">}</span><span class="sy0">,</span>
	 *     <span class="br0">]</span>
	 * <span class="br0">}</span>
	 * </pre>
	 * @throws Exception
	 */
	public static String getPlaceSuggestion(String query, String region,
			String lat, String lng) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		if (StringUtils.isNotEmpty(region)) {
			params.put("region", region);
		}

		if (StringUtils.isBlank(query) && StringUtils.isNotEmpty(lat) && StringUtils.isNotEmpty(lat)) {
			params.put("location", lat + "," + lng);
		}
		params.put("output", "json");
		params.put("ak", baiduMapKey);
		return HttpKit.get(PLACE_SUGGESTION_URI, params);
	}
	
	@SuppressWarnings("unchecked")
	public static List<SearchResult> getPlaceSuggestion(String city,String query,Double lat, Double lng) {
		city = StringUtils.isBlank(city)?"杭州":city;
		String suggest = getPlaceSuggestion(query, city, lat.toString(), lng.toString());
		Object object = JSON.parseObject(suggest).get("result");
		if (object != null) {
			StringBuffer sb  = new StringBuffer();
			List<SearchResult> list = new ArrayList<SearchResult>();
			String jsonResult = object.toString();
			List<BaiduMap> maps = JSON.parseArray(jsonResult, BaiduMap.class);
			for (int i  = 0 ; i < maps.size() ; i++) {
				BaiduMap m = maps.get(i);
				if(StringUtils.isBlank(m.getUid())){
					continue;
				}
				SearchResult a = new SearchResult();
				a.setName(m.getName());
				a.setDistrict(m.getDistrict());
				sb.append(m.getUid()).append(",");
				a.setAddress(m.getDistrict());
				a.setDistance("");
				Gps location = m.getLocation();
				if (location != null) {
					a.setLat(location.getLat() + "");
					a.setLng(location.getLng() + "");
				}else{
					a.setLat("0");
					a.setLng("0");
				}
				list.add(a);
				if((i+1)==maps.size()){
					sb.deleteCharAt(sb.lastIndexOf(","));
				}
			}
			try{
				String a = getPlaceDetail(sb.toString());
				JSONObject obj  =  JSON.parseObject(a);
				String status = obj.getString("status");
				if(StringUtils.equals("0", status)){
					JSONArray buildingArray = obj.getJSONArray("result");
					for(int i = 0 ; i < buildingArray.size(); i++){
						JSONObject addObj  = buildingArray.getJSONObject(i);
						String address = addObj.getString("address");
						if(StringUtils.isNotBlank(address)){
							list.get(i).setAddress(address);
						}
					}
				}
			}catch(Exception e){
				logger.info("百度地址uids获取详细地址失败，uids="+sb.toString());
			}
			return list;
		}
		return Collections.EMPTY_LIST;
	}
	
	public static String getPlaceDetail(String uid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("uids", uid);
		params.put("scope", "1");
		params.put("output", "json");
		params.put("ak", baiduMapKey);
		String str = HttpKit.get(PLACE_DETAIL, params);
		return str;
	}
	
	
	/**
	 * 取得最优地址
	 * 
	 * @param lat
	 *            纬度
	 * @param lng
	 *            经度
	 * @return
	 * @throws Exception
	 */
	public static String getZuiyou(String lat, String lng) {
		String results;
		try {
			results = circlePlaceSearch(lat, lng);
			JSONObject object = JSON.parseObject(results);
			if ("0".equals(object.getString("status"))
					&& object.getInteger("total") > 0) {
				JSONArray signBuilds = JSONArray.parseArray(object
						.getString("results"));
				return ((JSONObject) signBuilds.get(0)).getString("name");
			}
		} catch (Exception e) {
			logger.error("获取最优地址出错：" + e.getMessage());
		}
		return null;
	}

	/**
	 * 周边圆形区域检索
	 * 
	 * @param lat
	 *            纬度
	 * @param lng
	 *            经度
	 * @param query
	 *            检索关键字
	 * @param radius
	 *            周边检索半径，单位为米
	 * @return <pre id="queryResults" style="display: block;">
	 * {
	 * 	status : 0,
	 * 	message : "ok",
	 * 	total : 400,
	 * 	results : 
	 * 	[
	 * 		{
	 * 			name : "中国工商银行(上地支行)",
	 * 			location : 
	 * 			{
	 * 				lat : 40.031111,
	 * 				lng : 116.32052
	 * 			},
	 * 			address : "北京市海淀区上地信息路38号1号楼一层103号",
	 * 			street_id : "5c56edd68a44a627a1d2303a",
	 * 			telephone : "(010)62968329,82783096,82783238",
	 * 			detail : 1,
	 * 			uid : "5c56edd68a44a627a1d2303a"
	 * 		},
	 * 	]
	 * }
	 * </pre>
	 */
	public static String circlePlaceSearch(String lat, String lng) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", "房地产");// 检索条件
		params.put("location", lat + "," + lng);
		params.put("filter", "sort_name:distance");// 根据距离排序
		params.put("page_size", "10");
		params.put("scope", "2");// 返回检索POI详细信息
		params.put("radius", "2000");// 检索范围
		params.put("output", "json");
		// params.put("ak", "16005cc9538518f22a3b1c48fe36ea96");
		params.put("ak", baiduMapKey);
		return HttpKit.get(CIRCLE_PLACE_RUI, params);
	}

	/**
	 * 周边地理位置转换
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static List<SearchResult> circlePlaceSearch(Double lat, Double lng) {
		List<SearchResult> ret;
		try {
			ret = new ArrayList<>();
			String location = circlePlaceSearch(lat.toString(), lng.toString());
			JSONObject object = JSON.parseObject(location);
			if (object.getString("status").equals("0") && object.getInteger("total") > 0) {
				List<SignBuild> jsonarray = JSON.parseArray(object.getString("results"),SignBuild.class);
				String district = getDistrict(new Gps(lat, lng));
				for (SignBuild s:jsonarray) {
					SearchResult a = new SearchResult();
					a.setName(s.getName());
					a.setDistance("距离" + s.getDetail_info().getDistance() + "米");
					a.setDistrict(district);
					a.setAddress(s.getAddress());
					a.setLat(s.getLocation().getLat() + "");
					a.setLng(s.getLocation().getLng() + "");
					ret.add(a);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * 地址解析，由详细到街道的结构化地址得到百度经纬度信息 //带回调函数的返回格式 <br>
	 * Geocoding API包括地址解析和逆地址解析功能。<br>
	 * 地理编码：即地址解析，由详细到街道的结构化地址得到百度经纬度信息，且支持名胜古迹、标志性建筑名称直接解析返回百度经纬度。<br>
	 * 例如：“北京市海淀区中关村南大街27号”地址解析的结果是“lng:116.31985,lat:39.959836”，“百度大厦”地址解析的结果是“
	 * lng:116.30815,lat:40.056885”
	 * 逆地理编码，即逆地址解析，由百度经纬度信息得到结构化地址信息。例如：“lat:31.325152
	 * ,lng:120.558957”逆地址解析的结果是“江苏省苏州市虎丘区塔园路318号”。
	 * 
	 * <pre>
	 * //带回调函数的返回格式  
	 * showLocation&amp;&amp;showLocation( 
	 *  {
	 *  status: 0,
	 *  result: 
	 *  {
	 *  location: 
	 *  {
	 *  lng: 116.30814954222,
	 *  lat: 40.056885091681
	 *  },
	 *  precise: 1,
	 *  confidence: 80,
	 *  level: "商务大厦"
	 *  }
	 *  }
	 * )
	 * 
	 * //不带回调函数的返回值
	 * {
	 *  status: 0,
	 *  result: 
	 *  {
	 *  location: 
	 *  {
	 *  lng: 116.30814954222,
	 *  lat: 40.056885091681
	 * },
	 * precise: 1,
	 * confidence: 80,
	 * level: "商务大厦"
	 * }
	 * }
	 * </pre>
	 * 
	 * @param address
	 * @param city
	 * @return
	 * @throws Exception
	 */
	public String getGeocoder(String address, String city) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("address", address);
		params.put("city", city);
		params.put("output", "json");
		params.put("ak", baiduMapKey);
		return HttpKit.get(GEOCODING_URI, params);
	}

	/**
	 * 地址解析，由详细到街道的结构化地址得到百度经纬度信息 //带回调函数的返回格式 <br>
	 * Geocoding API包括地址解析和逆地址解析功能。<br>
	 * 地理编码：即地址解析，由详细到街道的结构化地址得到百度经纬度信息，且支持名胜古迹、标志性建筑名称直接解析返回百度经纬度。<br>
	 * 例如：“北京市海淀区中关村南大街27号”地址解析的结果是“lng:116.31985,lat:39.959836”，“百度大厦”地址解析的结果是“
	 * lng:116.30815,lat:40.056885”
	 * 逆地理编码，即逆地址解析，由百度经纬度信息得到结构化地址信息。例如：“lat:31.325152
	 * ,lng:120.558957”逆地址解析的结果是“江苏省苏州市虎丘区塔园路318号”。
	 * 
	 * <pre>
	 * //带回调函数的返回格式  
	 * showLocation&amp;&amp;showLocation( 
	 *  {
	 *  status: 0,
	 *  result: 
	 *  {
	 *  location: 
	 *  {
	 *  lng: 116.30814954222,
	 *  lat: 40.056885091681
	 *  },
	 *  precise: 1,
	 *  confidence: 80,
	 *  level: "商务大厦"
	 *  }
	 *  }
	 * )
	 * 
	 * //不带回调函数的返回值
	 * {
	 *  status: 0,
	 *  result: 
	 *  {
	 *  location: 
	 *  {
	 *  lng: 116.30814954222,
	 *  lat: 40.056885091681
	 * },
	 * precise: 1,
	 * confidence: 80,
	 * level: "商务大厦"
	 * }
	 * }
	 * </pre>
	 * 
	 * @param address
	 * @param city
	 * @return
	 * @throws Exception
	 */
	public static String getAddressComponent(String lat, String lng) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("location", lat + "," + lng);
		params.put("output", "json");
		params.put("ak", baiduMapKey);
		return HttpKit.get(GEOCODING_URI, params);
	}

	/**
	 * 获取详细地址
	 */
	public static String getAddress(String jsonstr) {
		try {
			JSONObject jsonObj = JSONObject.parseObject(jsonstr);
			return jsonObj.getJSONObject("result").getString("address");
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * 根据gps获取城市名称
	 * 
	 * @param gps
	 * @return
	 */
	public static String getCity(Gps gps) {
		String ret = BMapUtil.getAddressComponent(gps.getLat() + "",
				gps.getLng() + "");
		try {
			JSONObject jsonObj = JSONObject.parseObject(ret);
			JSONObject addressComponent = jsonObj.getJSONObject("result")
					.getJSONObject("addressComponent");
			return addressComponent.getString("city");
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 获取区坐标
	 */
	public static String getDistrict(Gps gps) {
		String ret = BMapUtil.getAddressComponent(gps.getLat() + "",
				gps.getLng() + "");
		try {
			JSONObject jsonObj = JSONObject.parseObject(ret);
			JSONObject addressComponent = jsonObj.getJSONObject("result")
					.getJSONObject("addressComponent");
			return addressComponent.getString("district");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 坐标转换
	 * 
	 * @param lat
	 *            维度坐标
	 * @param lng
	 *            经度坐标
	 * @param from
	 *            源坐标类型： 取值为如下：1：GPS设备获取的角度坐标;2：GPS获取的米制坐标、sogou地图所用坐标;
	 *            3：google地图、soso地图、aliyun地图、mapabc地图和amap地图所用坐标
	 *            4：3中列表地图坐标对应的米制坐标;5：百度地图采用的经纬度坐标;
	 *            6：百度地图采用的米制坐标;7：mapbar地图坐标;8：51地图坐标
	 * @param to
	 *            目的坐标类型 有两种可供选择：5：bd09ll(百度经纬度坐标),6：bd09mc(百度米制经纬度坐标);
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static String geoconvGps(String lat, String lng, String from,
			String to) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("coords", lng + "," + lat);
		params.put("from", from);
		params.put("to", to);
		params.put("ak", baiduMapKey);
		String result = HttpKit.get(PLACE_GEOCONV_URI, params);
		return result;
	}

	/**
	 * 地址转换<br>
	 * 微信GPS地址转换为百度GPS地址
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Gps geoconvGps(Double lat, Double lng) {
		lat = lat == null ? 0 : lat;
		lng = lng == null ? 0 : lng;
		Gps gps = GpsCorrect.transform(lat, lng);
		String geoconv = geoconvGps("" + gps.getLat(), "" + gps.getLng(), "3",
				"5");
		JSONObject map = JSONObject.parseObject(geoconv);
		if ("0".equals(map.getString("status"))) {
			JSONObject js = (JSONObject) ((List) map.get("result")).get(0);
			gps.setLat(js.getDoubleValue("y"));
			gps.setLng(js.getDoubleValue("x"));
			return gps;
		}
		return null;
	}

	/**
	 * 计算两点之间距离
	 * 
	 * @param start
	 * @param end
	 * @return 米
	 */
	public static double getDistance(Gps start, Gps end) {
		double lat1 = (Math.PI / 180) * start.getLat();
		double lat2 = (Math.PI / 180) * end.getLat();
		double lon1 = (Math.PI / 180) * start.getLng();
		double lon2 = (Math.PI / 180) * end.getLng();
		// 地球半径
		double R = 6371.393;
		// 两点间距离 km，如果想要米的话，结果*1000就可以了
		double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.cos(lon2 - lon1))
				* R;
		return d * 1000;
	}

	/**
	 * 计算点是不是在多边形内
	 * 
	 * @param gps
	 *            gps
	 * @param gpsList
	 *            gps数据
	 * 
	 * @return
	 */
	public static boolean isPointInPolygon(Gps gps, List<Gps> gpsList) {
		ArrayList<Double> polygonXA = new ArrayList<Double>();
		ArrayList<Double> polygonYA = new ArrayList<Double>();
		for (Gps g : gpsList) {
			polygonXA.add(g.getLng());
			polygonYA.add(g.getLat());
		}
		return isPointInPolygon(gps.getLng(), gps.getLat(), polygonXA,
				polygonYA);
	}

	/**
	 * 
	 * @param gpsS
	 *            JSON格式的string对象lat=30.2222，lng=120.2222
	 * @param gpsListS
	 *            JSON格式的string对象
	 * @return
	 */
	public static String getSignBuild(Gps gpsS, String gpsListS) {
		List<Gps> gpsL = JSONArray.parseArray(gpsListS, Gps.class);
		if (isPointInPolygon(gpsS, gpsL)) {// 给定的GPS在范围内，则查找最优地址
			return getZuiyou("" + gpsS.getLat(), "" + gpsS.getLng());
		}
		return null;
	}

	/**
	 * 计算点是不是在多边形内
	 * 
	 * @param px
	 *            经度
	 * @param py
	 *            纬度
	 * @param polygonXA
	 *            经度数组
	 * @param polygonYA
	 *            纬度数组
	 * @return
	 */
	private static boolean isPointInPolygon(double px, double py,
			ArrayList<Double> polygonXA, ArrayList<Double> polygonYA) {
		boolean isInside = false;
		double ESP = 1e-9;
		int count = 0;
		double linePoint1x;
		double linePoint1y;
		double linePoint2x = 180;
		double linePoint2y;

		linePoint1x = px;
		linePoint1y = py;
		linePoint2y = py;

		for (int i = 0; i < polygonXA.size() - 1; i++) {
			double cx1 = polygonXA.get(i);
			double cy1 = polygonYA.get(i);
			double cx2 = polygonXA.get(i + 1);
			double cy2 = polygonYA.get(i + 1);
			if (isPointOnLine(px, py, cx1, cy1, cx2, cy2)) {
				return true;
			}
			if (Math.abs(cy2 - cy1) < ESP) {
				continue;
			}

			if (isPointOnLine(cx1, cy1, linePoint1x, linePoint1y, linePoint2x,
					linePoint2y)) {
				if (cy1 > cy2)
					count++;
			} else if (isPointOnLine(cx2, cy2, linePoint1x, linePoint1y,
					linePoint2x, linePoint2y)) {
				if (cy2 > cy1)
					count++;
			} else if (isIntersect(cx1, cy1, cx2, cy2, linePoint1x,
					linePoint1y, linePoint2x, linePoint2y)) {
				count++;
			}
		}
		if (count % 2 == 1) {
			isInside = true;
		}

		return isInside;
	}

	private static double Multiply(double px0, double py0, double px1,
			double py1, double px2, double py2) {
		return ((px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0));
	}

	private static boolean isPointOnLine(double px0, double py0, double px1,
			double py1, double px2, double py2) {
		boolean flag = false;
		double ESP = 1e-9;
		if ((Math.abs(Multiply(px0, py0, px1, py1, px2, py2)) < ESP)
				&& ((px0 - px1) * (px0 - px2) <= 0)
				&& ((py0 - py1) * (py0 - py2) <= 0)) {
			flag = true;
		}
		return flag;
	}

	private static boolean isIntersect(double px1, double py1, double px2,
			double py2, double px3, double py3, double px4, double py4) {
		boolean flag = false;
		double d = (px2 - px1) * (py4 - py3) - (py2 - py1) * (px4 - px3);
		if (d != 0) {
			double r = ((py1 - py3) * (px4 - px3) - (px1 - px3) * (py4 - py3))
					/ d;
			double s = ((py1 - py3) * (px2 - px1) - (px1 - px3) * (py2 - py1))
					/ d;
			if ((r >= 0) && (r <= 1) && (s >= 0) && (s <= 1)) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 百度地图通用返回内容
	 * 
	 * @author zxl0047
	 *
	 * @param <T>
	 */
	public class BdMap<T> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -881207101208252483L;
		private String status;
		private String message;
		private T result;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public T getResult() {
			return result;
		}

		public void setResult(T result) {
			this.result = result;
		}
	}

	/**
	 * 百度坐标经纬度
	 * 
	 * @author zxl0047
	 *
	 */
	public class Baidugeoconv implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8360669028462160211L;
		/**
		 * 经度
		 */
		private double x;
		/**
		 * 纬度
		 */
		private double y;

		/**
		 * 经度
		 */
		public double getX() {
			return x;
		}

		/**
		 * 经度
		 */
		public void setX(double x) {
			this.x = x;
		}

		/**
		 * 纬度
		 */
		public double getY() {
			return y;
		}

		/**
		 * 纬度
		 */
		public void setY(double y) {
			this.y = y;
		}
	}

	/**
	 * gps纠偏算法，适用于google,高德体系的地图
	 * 
	 * @author wukewang
	 *
	 */
	public static class GpsCorrect {
		final static double pi = 3.14159265358979324;
		final static double a = 6378245.0;
		final static double ee = 0.00669342162296594323;

		/**
		 * GPS纠偏算法
		 * 
		 * @param lat
		 *            纬度
		 * @param lng
		 *            经度
		 * @param latlng
		 */
		public static Gps transform(double lat, double lng) {
			Gps gps = new Gps();
			if (outOfChina(lat, lng)) {
				gps.setLat(lat);
				gps.setLng(lng);
				return gps;
			}
			double dLat = transformLat(lng - 105.0, lat - 35.0);
			double dLon = transformLon(lng - 105.0, lat - 35.0);
			double radLat = lat / 180.0 * pi;
			double magic = Math.sin(radLat);
			magic = 1 - ee * magic * magic;
			double sqrtMagic = Math.sqrt(magic);
			dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
			dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
			gps.setLat(lat + dLat);
			gps.setLng(lng + dLon);
			return gps;
		}

		/**
		 * 判断是否超出中国范围
		 * 
		 * @param lat
		 *            纬度
		 * @param lng
		 *            经度
		 * @return
		 */
		private static boolean outOfChina(double lat, double lng) {
			if (lng < 72.004 || lng > 137.8347)
				return true;
			if (lat < 0.8293 || lat > 55.8271)
				return true;
			return false;
		}

		/**
		 * 纬度转换
		 * 
		 * @param x
		 *            经度
		 * @param y
		 *            纬度
		 * @return
		 */
		private static double transformLat(double x, double y) {
			double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
					+ 0.2 * Math.sqrt(Math.abs(x));
			ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x
					* pi)) * 2.0 / 3.0;
			ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
			ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi
					/ 30.0)) * 2.0 / 3.0;
			return ret;
		}

		/**
		 * 经度转换
		 * 
		 * @param x
		 *            经度
		 * @param y
		 *            纬度
		 * @return
		 */
		private static double transformLon(double x, double y) {
			double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
					* Math.sqrt(Math.abs(x));
			ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x
					* pi)) * 2.0 / 3.0;
			ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
			ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
					* pi)) * 2.0 / 3.0;
			return ret;
		}
	}
	
	public static void main(String[] args) {
		
		getPlaceSuggestion("杭州","双城国际", 30.21303400000, 120.20805000000);
	}
}
