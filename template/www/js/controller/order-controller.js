/**
 * 确认下单页相关数据调用
 */
ctrl.controller('corderCtrl', [ '$scope','$http','$routeParams','$compile','$location','orderService','userService',
	function($scope,$http,$routeParams,$compile,$location,orderService,userService) {
	    /**
	     * 取得默认地址
	     */
		userService.getDefAddress().success(function(d) {
			$scope.address=d.body;
			/**
		    * 购物车转换成订单列表
		    */
			orderService.getOrder($routeParams.sellerId).success(function(c) {
			  $scope.order=c.body;
			  //console.log($scope.order);
			  //console.log("aaa");
			  //console.log(c.body);
			  /**
			   * 判断所购买的商品金额是否可以使用优惠券，如果达到满减要求则可以使用，否则不可使用
			   * @param redId 红包ID
			   * @param redName 显示的名称
			   */
			  //$scope.red = c.body.actCart[0];
			  $scope.redFn = function(red,defaultornot){
				  if(red!=undefined){
					  if(red.sums<=c.body.goodAmount){
						  $scope.order.redId = red.id;
						  $scope.order.redName = red.name;  
						  $scope.order.discountAmount=red.balance;
						  if(defaultornot&&defaultornot==true){
							  $scope.defaultshow=1;
						  }else{
							  $scope.defaultshow=0;
						  }						  
					  }else{
						  $scope.order.redId=undefined;
						  $scope.order.redName='不使用优惠券';
						  $scope.order.discountAmount=0;
						  $scope.defaultshow=0;
					  }
				  }else{
					  $scope.order.redId=undefined;
					  $scope.order.redName='不使用优惠券';
					  $scope.order.discountAmount=0;
					  $scope.defaultshow=0;
				  }				   
			  }
			  $scope.redFn($scope.red,false);
			  
			  if($scope.address!=undefined){
				  $scope.order.addressId=$scope.address.id;
			  }
			  $scope.order.orderSendTime=$scope.order.bestTime;
			  var ogoods = c.body.orderGoods;
			  $scope.ogoodslen = 0;
			  for(var i = 0; i<ogoods.length; i++){
				  $scope.ogoodslen = $scope.ogoodslen + ogoods[i].goodsNumber;
			  }
			  /**
			   * 选择配送方式,0是正常配置，1自提，如果
			   */
			  $scope.shippingAmount = $scope.order.shippingAmount;
			  $scope.cType = function(t){
				$scope.order.shippingType = t;
				if(t==0){
					$scope.order.shippingAmount = $scope.shippingAmount;
				}else{
					$scope.order.shippingAmount = 0;
				}
			  };
			  /**
			   * 确认提交
			   */
			  $scope.confirm_C = function(shippingType){
				if($scope.order.orderGoods==undefined||$scope.order.orderGoods.length<=0){
					alert("没有选择购物车，请选择购物车信息");
					$location.path("/cart");
				}
				orderService.confirm($routeParams.sellerId, $scope.order).then(function(data) {
					if(data.success){
						window.location.href=data.body;
					}else{
						alert(data.message);
					}
				});
			  };
		   })
		})
	   /**
	    * 获取商家信息
	    */
		$http.get(ctx+"v3/seller/detail/"+$routeParams.sellerId).success(function(data) {
		    $scope.seller = data.body;
		    //console.log("selleraddress");
		    //console.log(data.body);
		});
		/**
		 * 弹窗相关模块Fn
		 */
		$scope.showPops = function(directive){
			//console.log($routeParams.sellerId);
		    $scope.order.sellerId=$routeParams.sellerId;
			angular.element(document.getElementById("ng-view")).append($compile("<"+directive+"></"+directive+">")($scope));			
		};
	} 
]);