package com.xyl.huala.wechat.v3.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.xyl.huala.entity.HSellerGoodsExtends;

/**   
 * @tag
 */
public class PropUtil {
	
	private static GoodsProp changeGoodsProp(HSellerGoodsExtends hSellerGoodsExtends){
		String[] dataKey = hSellerGoodsExtends.getDataKey().split(":");
		String[] dataValue = hSellerGoodsExtends.getDataValue().split(":");
		PropUtil.GoodsProp goodsProp = new PropUtil().new GoodsProp();
		goodsProp.setId(hSellerGoodsExtends.getDataKey());
		goodsProp.setPid(dataKey[0]);
		goodsProp.setVid(dataKey[1]);
		goodsProp.setpName(dataValue[0]);
		goodsProp.setvName(dataValue[1]);
		return goodsProp;
	}
	
	
	public static String toPropString(Collection<HSellerGoodsExtends> coll){
		if(CollectionUtils.isEmpty(coll)){
			return null;
		}
		Map<String,StringBuffer> map = new HashMap<String,StringBuffer>();
		for(HSellerGoodsExtends prop : coll){
			GoodsProp goodsProp = changeGoodsProp(prop);
			StringBuffer sb = map.get(goodsProp.getPid());
			if(sb != null){
				sb.append(" ").append(goodsProp.getvName());
			}else{
				sb = new StringBuffer();
				sb.append(goodsProp.getpName()).append(":").append(goodsProp.getvName());
			}
			map.put(goodsProp.getPid(), sb);
		}
		StringBuffer ret = new StringBuffer();
		Iterator iterator = map.entrySet().iterator();;
		while(iterator.hasNext()){
			Map.Entry entry = (Map.Entry) iterator.next(); 
			ret.append(entry.getValue()).append(" ").append("\n");
		}
		return ret.toString();
	}
	
	
	public static Map<String,StringBuffer> toPropMap(Collection<HSellerGoodsExtends> coll){
		if(CollectionUtils.isEmpty(coll)){
			return null;
		}
		Map<String,StringBuffer> map = new LinkedHashMap<String,StringBuffer>();
		for(HSellerGoodsExtends prop : coll){
			GoodsProp goodsProp = changeGoodsProp(prop);
			StringBuffer sb = map.get(goodsProp.getpName());
			if(sb != null){
				sb.append(" ").append(goodsProp.getvName());
			}else{
				sb = new StringBuffer();
				sb.append(goodsProp.getvName());
			}
			map.put(goodsProp.getpName()+":", sb);
		}
		return map;
	}
	
	public  class GoodsProp {
		
		private String id;
		
		private String pid;
		
		private String vid;
		
		private String pName ;
		
		private String vName ;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

		public String getVid() {
			return vid;
		}

		public void setVid(String vid) {
			this.vid = vid;
		}

		public String getpName() {
			return pName;
		}

		public void setpName(String pName) {
			this.pName = pName;
		}

		public String getvName() {
			return vName;
		}

		public void setvName(String vName) {
			this.vName = vName;
		}
		
	}
}
