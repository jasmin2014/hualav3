

/**
 * 购物车控制器
 */
ctrl.controller('cartCtrl', [ '$scope', '$http','cartService','$location','userService',
	function($scope, $http, cartService,$location,userService){
		/**
		 * 获取地址
		 */
		userService.getDefAddress().success(function(d) {
			$scope.address=d.body;
		});
		/**
	     * 获取购物车列表
	     */
		cartService.getListBySeller().success(function(data) {
			$scope.cartList=data.body;	
			//console.log(data.body);
		});
		/**
		 * 变更购物车
		 */
	    $scope.changeCart=function(goods,goodsList){
	    	//console.log(goods);
	    	if(goods.goodNum<=0){
	    		cartService.getListBySeller().success(function(data) {
	    			$scope.cartList=data.body;
	    			//console.log(data.body);
	    		});
	    	}
	    	goodsList.total=0;
	    	goodsList.cartList.forEach(function(e) {
	    		if(e.choose){
	    			goodsList.total=goodsList.total+e.goodNum*e.salePrice;
	    		}
	    	});
	    	//console.log(goodsList.total);
	    }
		/**
		 * 全部选择购物车信息
		 */
		$scope.chooseAll=function(goodsList){
			cartService.choose(goodsList.sellerId,!goodsList.chooseAll).success(function(data) {
				var total=0;
				//console.log(data);
				if(data.success){
					goodsList.chooseAll=data.body;
					goodsList.cartList.forEach(function(g){
						g.choose =data.body;
						if(g.choose){
							total+=g.goodNum*g.salePrice
						}
					});			
					goodsList.total=total;
				}
			});
		}
		/**
		 * 选中购物车
		 */
		$scope.choose = function(goodsList,goods){
			cartService.choose(goodsList.sellerId,!goods.choose,goods.skuId).success(function(data) {
				if(data.success){
					var total=0;
					goods.choose = data.body;
					var chooseAll=true;
					goodsList.cartList.forEach( function(g) {
						if(!g.choose){
							chooseAll=false;
							//return;
						}
						if(g.choose){
							total+=g.goodNum*g.salePrice
						}
					});		
					goodsList.total=total;
					goodsList.chooseAll=chooseAll;					
				}
			})
		}
		/**
		 * 跳转到确认下单
		 * @param 店铺ID
		 */
		$scope.confirmOrder=function(sellerId){
				$location.path("/order/confirm/"+sellerId);
		}
	}
]);