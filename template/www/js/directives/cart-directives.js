/**
 * 增加购物车
 * 传入的值为商品信息
 */
app.directive('addCart', ['$rootScope','cartService',function ($rootScope,cartService) {
    return {
      scope: {
    	  goods:"=",
    	  sellerId:"=",
    	  fun:"&"
      },
      link:function(scope, element, attrs){    	  
    	  element.on("click", function(event) {
    		  cartService.add(scope.sellerId,scope.goods.skuId).success(function(d) {
	    			if(d.success){
	    				scope.goods.goodNum=d.body.goodNum;
	 					$rootScope.$broadcast("cartIcon", true);
	 					if(scope.fun!=undefined){
		    			  scope.fun();
		    		    }
	 					if(attrs.pagename&&attrs.pagename=="detail"){
	 						angular.element(document.getElementById("ng-view")).append('<div class="finishedwarn" id="finished">加入购物车成功</div>')
	 					}
	 					setTimeout(function(){
	 						angular.element(document.getElementById("finished")).remove();
	 					},1500);
	    			}else{
	    				alert(d.message);
	    			} 					
 			 });    		  
    	 })
      }              
    }
  }]);
/**
 * 减少购物车信息，如果减少到购物车为0时，则广播消息，清理购物车
 */
app.directive('reduceCart', ['$rootScope','cartService',function ($rootScope,cartService) {
	
    return {
      scope: {
    	  goods:"=",
    	  sellerId:"=",
    	  fun:"&"
      },
      link:function(scope, element, attrs){
    	  element.on("click", function(event) {
    		  cartService.reduce(scope.sellerId,scope.goods.skuId).success(function(d) {
 					scope.goods.goodNum=d.body.goodNum;
 					$rootScope.$broadcast("cartIcon", true);
 					if(scope.fun!=undefined){
 		    		  scope.fun();
 		    		}
 			  });
    		 
    	 })
      }              
    }
  }]);
