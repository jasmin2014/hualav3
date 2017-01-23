/**
 * 定位服务
 */
services.factory('userService',['$http','$q', function($http, $q) {
	return {
		getAddressList : function() {
			return $http.get(ctx + "v3/address/list");
		},
		/**
		 * 添加地址
		 * @param type get获取，add增加，del删除，update更新
		 */
		address : function(type,id,data) {
			return $http.post(ctx + "v3/address/"+type+"/"+id,data);
		},
		/**
		 * 获取地址信息
		 */
		gpsAddress : function(keyword,lat,lng) {
			//console.log(keyword,lat,lng);
			return $http({method:'get',url:ctx + "v3/address/search",params:{kw:keyword,lat:lat,lng:lng}});
		},
		/**
         * 取得默认地址
         */
		getDefAddress:function(){
        	return $http.get(ctx+"v3/address/default");
        },
        /**
         * 卡券管理
         */
		getcoupons : function(){
			return $http.get(ctx+"v3/coupon/view");
		},
		/**
		 * 收藏信息
		 */
		getcollects : function(){
			return $http.get(ctx+"v3/collects");
		},
		/**
		 * 店铺收藏
		 */
		postcollect : function(sid){
			return $http.post(ctx + 'v3/collects/'+sid);
		},
		/**
		 * 获取用户扫码的接口 返回商家和活动信息
		 */
		scanPay : function(sellerId){
			return $http.get(ctx + 'v3/scan_pay/' + sellerId);
		},
		/**
		 * 确认支付
		 */
		scanGoPay : function(sellerId, payAmount, amount, actCardId, actUserId, isWxScan){
			return $http({method:'post',url:ctx + "v3/scan_go_pay/" + sellerId,params:{payAmount:payAmount,
				amount:amount, actCardId:actCardId, actUserId:actUserId, isWxScan:isWxScan}});
		},
		/**
		 * 支付成功之后跳转到支付页面
		 */
		scanPaySuccess : function(orderId){
			return $http.get(ctx + 'v3/scan_pay_success/' + orderId);
		},
		/**
		 * 调用去支付的接口
		 */
		goPay : function(orderId) {
			// 微信支付
			return $http.get(ctx+ "pay_req/" + orderId);
		}
	}
}]);
