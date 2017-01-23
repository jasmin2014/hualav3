
/**
* 确认下单数据
**/
services.service('orderService', [ '$http','$q', 
    function($http,$q) {
		/**
		 * 根据购物车获取订单信息
		 */
		this.getOrder=function(sellerId){
			return $http.get(ctx+"v3/order/confirm/"+sellerId);
		}
		/**
		 * 订单提交
		 */
		this.confirm=function(sellerId,order){
			var defer=$q.defer();
			var s=base64encode(utf16to8(angular.toJson(order)));
			 $http.post(ctx+"v3/order/confirm/"+sellerId,s).success(function(data) {
			 	if(data.success){
			 		$http.get(ctx+"pay/"+data.body,{params: {'path':'v3'}}).success(function(c) {
			 			defer.resolve(c);
			 		})
			 	}else{
			 		defer.resolve(data);
			 	}
			 });
			 return defer.promise;
		}
		/**
		 * 根据店铺ID取得预约时间
		 */
        this.getChooseDate = function(sellerId){
        	return $http.get(ctx+"v3/order/choosedate/"+sellerId);
        };
        
    }
])