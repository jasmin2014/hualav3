package com.xyl.huala.wechat.v3.domain;

import com.xyl.huala.entity.HSellerSkuProp;

/**   
 * @tag
 */
public class SellerSkuProp extends HSellerSkuProp{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8947749781192198567L;

	/**
	 * sku的销售属性组合字符串（颜色，大小，等等，可通过类目API获取某类目下的销售属性）,格式是红色 XXL
	 */
	public String getPropName() {
		if(super.getProperties() != null){
			StringBuffer sb  = new StringBuffer();
			String[] props = super.getProperties().split(";");
			try{
				for(String prop : props){
					sb.append(prop.split(":")[2]+",");
				}
				sb.deleteCharAt(sb.lastIndexOf(","));
			}catch(Exception e){
			}
			return sb.toString();
		}
		return super.getProperties();
	}
}
