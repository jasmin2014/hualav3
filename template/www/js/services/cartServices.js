/**
 * 财物车处理
 */
services.service('cartService', [ '$http', '$q','$rootScope', function($http, $q,$rootScope) {
	/**
	 * 增加购物车信息
	 */
	this.add = function(sellerId, skuId) {
		return $http.post(ctx + "v3/cart/add", {
			'sellerId' : sellerId,
			'skuId' : skuId
		});
	}
	/**
	 * 减少购物车信息
	 * 
	 * @param sellerId
	 *            店铺ID
	 * @param goodsSkuId
	 *            商品的SKUID
	 */
	this.reduce = function(sellerId, skuId) {
		return $http.post(ctx + "v3/cart/reduce", {
			'sellerId' : sellerId,
			'skuId' : skuId
		});
	}
	/**
	 * 查找购物车信息
	 */
	this.get = function(sellerId, skuId) {
		return $http.get(ctx + "v3/cart/get", {
			'sellerId' : sellerId,
			'skuId' : goodsSkuId
		});
	}
	/**
	 * 查找购物车信息, <br>
	 * 1、ng-app初始化时会调用，初始化系统中的小红点 <br>
	 * 2、购物车列表中会调用
	 */
	this.getList = function(sellerId) {
		var defer=$q.defer();
		$http.get(ctx + "v3/cart/list", {'sellerId' : sellerId}).success(function(data) {
			if(data.body!=undefined&&data.body.length>0){
				$rootScope.$broadcast('cartIcon',true);
			}else{
				$rootScope.$broadcast('cartIcon',false);
			}
			defer.resolve(data);
		});
		return defer.promise;
	}
	/**
	 * 購物車列表,按店鋪分組
	 */
	this.getListBySeller = function() {
		return $http.get(ctx + "v3/cart/seller-list")
	}
	/**
	 * 选择购物车
	 */
	this.choose = function(sellerId,choose,skuId) {
		return $http.get(ctx + "v3/cart/choose/"+sellerId,{params:{'skuId':skuId,'choose':choose}});
	}
	/**
	 * 查找购物车信息
	 */
	this.getCart = function(cartList,sellerId, skuId) {
		var num;
		cartList.forEach(function(c) {
			if(c.sellerId==sellerId&&c.skuId==skuId){
				num= c.goodNum;
			}
		})
		return num;
	}
} ]);
