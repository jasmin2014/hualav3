/**
 * 订单列表页面
 */
ctrl.controller('orderCtrl', [ '$scope', '$http',
	function($scope, $http){
		/**
		 * 订单列表接口获取数据
		 */
		$scope.getList=function(isHistory){
			$scope.isHistory=isHistory;
			$http.get(ctx + "v3/order/list?isHistory="+isHistory).success(function(d){
				$scope.orderList = d.body;
				//console.log(d.body);
			});
		}
		
		$scope.getList(false);
	}
]);

/**
 * 订单详情页
 */
ctrl.controller('orderDetailCtrl', [ '$scope', '$http', '$routeParams','$route',
    function($scope, $http, $routeParams,$route){
		/**
		 * 订单详情接口获取数据
		 */
		$http.get(ctx + "v3/order/view/"+$routeParams.oid).success(function(d){
			$scope.orderDetail = d.body.order;
			$scope.orderDetail.seller=d.body.seller;
			//console.log(d.body);
		});
		/**
		 * 去支付
		 */
		$scope.goPay=function(id){
			$http.get(ctx+"pay/"+id,{params: {'path':'v3'}}).success(function(data) {
				if(data.success){
					window.location.href=data.body;
				}else{
					alert(data.message);
				}
	 		})
		};
		/**
		 * 订单处理：cancel取消订单：confirm：确认收货
		 */
		$scope.orderExec = function(orderStatus) {
			$http.get(ctx + "v3/order/orderStatus", {
				params : {
					'orderId' : $scope.orderDetail.id,
					'orderStatus' : orderStatus
				}
			}).success(function(data) {
				if (data.success) {
					$route.reload();
				} else {
					alert(data.message);
				}
			})
		}
	}
]);
/**
 * "我的"-页面
 */
ctrl.controller('myCtrl',['$scope','$http','$location','$route','authService',
  function($scope,$http,$location,$route,authService){	
	$scope.logout=function(){
		authService.logout().success(function(data) {
			if(data.success){
				$location.path("/seller");
			}
		});
	}
	/**
	 * 我的页面
	 */
	$http.get(ctx+"v3/center").success(function(data) {
		$scope.user=data.body;
	});
	/**
	 * 跳转到通知表态页面
	 */
	$scope.load=function(){
		window.location.href="http://mp.weixin.qq.com/s?__biz=MzAwNzYzNTYzOQ==&mid=504391207&idx=1&sn=713aba54307610d9c026a2273ebfbe5c#rd";
	}
	/**
	 * 默认隐藏意见反馈框
	 */
	$scope.showfeedback = $scope.showfeedpop = false;
	/**
	 * 意见反馈-判断所提交文案长度，如果所输入的长度大于15时，确认提交反馈，否则提示继续输入
	 */
	$scope.submittxt = function(){
		$scope.showfeedpop = true;
		var text = angular.element(document.getElementById('content')).val();
		if(text.length<15){
			$scope.invalid = true;  // invalid 无效时显示提示文案
			$scope.effective = false;  // effective 有效时显示确认提交文案
		}else{
			$scope.invalid = false;
			$scope.effective = true;
		}
	};
	/**
	 * 确认提交反馈信息
	 */
	$scope.submit = function(){
		var text = angular.element(document.getElementById('content')).val();
		$http.post(ctx + 'v3/feedback').success(function(){
			$route.reload();
		})
	}
  }                              
]);
/**
 * "我的"-红包管理
 */
ctrl.controller('myCouponCtrl',['$scope','userService',
  function($scope,userService){
	$scope.activeitem = false;
	/**
	 * 我的红包
	 */
	userService.getcoupons().success(function(data) {
		console.log(data.body);
		$scope.unused=[];
		$scope.used=[];
		if(data.body!=undefined){
			data.body.forEach(function(elt) {
				if(elt.isUse=='0'&&elt.isInvalid=='0'){
					$scope.unused.push(elt);
					
				}else{
					$scope.used.push(elt);
				}
			})
			//console.log($scope.unused);
		}
	})
  }                              
]);
/**
 * "我的"-地址管理页
 */
ctrl.controller('addressListCtrl',['$scope','$compile','$routeParams','$window','$cookieStore','userService',
  function($scope,$compile,$routeParams,$window,$cookieStore,userService){
		
		/**
		 * 获取已经添加过的地址列表
		 */
		userService.getAddressList().success(function(response){
			$scope.addressList = response.body;
			//console.log(response.body);
		});
		/**
		 * $scope.routeparam值来判断是编辑地址还是添加地址
		 */
		$scope.routeparam = $routeParams.type;
		/**
		 * 删除地址-弹窗二次确认是否删除
		 */
		$scope.del=function(aid){
			$scope.showdeletepop = true;
			$scope.addressid = aid;			
		}
		/**
		 * 编辑地址
		 */
		$scope.edit=function(aid){
			window.location.href="#/address/edit/"+aid;
		}
		/**
		 * 删除地址-确定删除
		 */
		$scope.delok=function(addressid){
			//console.log('addressid:'+addressid);
			userService.address("del",addressid).success(function(data) {
				if(data.success){
					$window.location.reload();
				}
			})
		};
		/**
		 * 地址管理页，点击编辑按钮时，出现编辑和删除两个按钮
		 */
		$scope.add=function(a,addressList){
			$scope.addressId=a.id;
		}
		/**
		 * 切换地址点击事件 address当前选中地址地址
		 */
		$scope.change=function(address,$event){
			if($event.stopPropagation){
				$event.stopPropagation();
			}
			if($routeParams.type=='change'){
				userService.address('update', address.id, address).success(function(data) {
					if(data.success){
						var gps=$cookieStore.get("gps");
						if(gps==undefined)gps={};
						gps.lng=address.lng;
						gps.lat=address.lat;
						gps.address=address.signBuilding;
						$cookieStore.put("gps",gps);
						sessionStorage.setItem("gps",JSON.stringify(gps));
						$window.history.back();
					}
				})
			};			
		}
		/**
		 * 点击定位获取当前地址，由于在首页上如果没有gps的cookie，则自动定位，所以这里我们将cookie清理，然后返回便可
		 */
		$scope.getGps=function(){
			$cookieStore.remove("gps");
			$window.history.back();		
		}
	}                    
]);
/**
 * "我的"-地址编辑or创建页
 */
ctrl.controller('addressEditCtrl',['$scope','$routeParams','$window','$compile','$cookieStore','userService',
  function($scope,$routeParams,$window,$compile,$cookieStore,userService){	
	/**
	 * 获取地址信息
	 */
	userService.address('get',$routeParams.id).success(function(response){
		//console.log(response);
		$scope.address = response.body;
	});

	/**
	 * 修改地址
	 */
	//console.log($routeParams.id);
	$scope.submit=function(){
		var type = '';
		if($routeParams.id!="-1"){
		  type="update";
		}else{
			type="add";
		}
		//console.log($scope.address);
		userService.address(type,$routeParams.id,$scope.address).success(function(data){
			//console.log(data);
			$scope.addressList = data.body;
			$window.history.back();			
		});
	}
	/**
	 * 搜索地址
	 */
	$scope.search = function(){
		//console.log($scope);
		angular.element(document.getElementById("ng-view")).append($compile('<address-search address=address></address-search>')($scope));		
	}
  }                              
]);
/**
 * "我的"收藏
 */
ctrl.controller('mycollectionCtrl',['$scope','$window','userService',
  function($scope,$window,userService){	
	/**
	 * 获取所有收藏过的店铺列表
	 */
	userService.getcollects().success(function(data) {
		$scope.clists = data.body;
		//console.log($scope.clists);
	});
	/**
	 * 设置当前要取消的店铺id为‘0’，collectid该字段主要用来做取消店铺收藏的交互
	 */
	$scope.collectid = '0';
	/**
	 * 手指滑动某个店铺时，显示删除的按钮,在指令：ng-swipe-left上面调用
	 */
	$scope.showdeleteFn = function(t,datas){
		datas.forEach(function(d){
			if(d.id==t.id){
				$scope.collectid = t.id;
			}
		})
	};
	/**
	 * 点击‘删除’按钮时，直接取消收藏掉当前的那条店铺记录
	 */
	$scope.cancelcollectFn = function(t){
		userService.postcollect(t.id).success(function(data){
			$window.location.reload();
		})
	}
  }                              
]);
/**
 * 扫码支付 Created by gly on 2016/05/09
 */
ctrl.controller('scanPayMentCtrl',['$scope','$rootScope','$routeParams','userService','authService','$cookieStore','$route',
function($scope,$rootScope,$routeParams,userService,authService,$cookieStore,$route){
	var gps=$cookieStore.get("gps");
	if(gps!=undefined){
		gps.sellerId=$routeParams.sellerId;
	}else{
		gps={sellerId:$routeParams.sellerId};
	}
	$cookieStore.put("gps", gps);
	$scope.isLogin = authService.isLogin();
	$scope.CanGotoPay = false;
	$scope.showNumberBoarad=true;	
	/**
	 * 获取用户扫码的接口 返回商家和活动信息
	 */
	//amount==''||amount=='0.00'||!sellerStatus
	userService.scanPay($routeParams.sellerId).success(function(data) {
		$scope.seller = data.body.seller;		
		$scope.card = data.body.card;
		$scope.content = data.body.content;
	});
	/**
	 * 是否是微信浏览器
	 */
	function isWeiXin(){ 
		var ua = window.navigator.userAgent.toLowerCase(); 
		if(ua.match(/MicroMessenger/i) == 'micromessenger'){ 
			return true; 
		}else{ 
			return false; 
		} 
	} 
	/**
	 * 确认支付
	 */
	$scope.amount='';
	$scope.scanGoPay = function(sid) {
		// 获取用户输入的金额
		var amount = $scope.amount;
		var payAmount = $scope.payAmount;
		// 获取卡券信息
		var card = $scope.card;
		// 卡券的id
		var actCardId = 0;
		// 红包的id
		var actUserId = 0;
		//console.log($scope.amount);
		if(card != undefined && card != null) {
			actCardId = card.actCartId;
			actUserId = card.id; 
		}
		// 判断是否是微信扫码
		var isWxScan = false;
		if(isWeiXin()) {
			isWxScan = true;
		}
		//&&$scope.sellerStatus!="1"
		if(amount!=0){
			userService.scanGoPay(sid, payAmount, amount, actCardId, actUserId, isWxScan).success(function(data){
				if(data.success) {
					if(isWxScan) {
						// 微信支付
						userService.goPay(data.body).success(function(d) {
							// 数据保存成功之后 跳转到支付的页面 
							if(d.success) {
								window.location.href = d.body;
							} else {
								alert(d.message);
							}
						});
					} else {
						// 支付宝支付
						window.location.href = ctx + "ali_pay_req/" + data.body; 
					}
				} else {
					// 红包领取完了 重新刷新页面
					alert(data.message);
					if(data.errorCode == 'red_package_is_empty') {
						$route.reload();
					}
				}
			});
		}/*else{
			alert("不能支付");
		}*/		
	};
	$scope.showkeyBoard = function(){
		$scope.showNumberBoarad=true;
		$scope.changeInput=false;
		/*$location.hash('bottom');
		$anchorScroll();*/
	}
	/**
	 * 弹出登录的页面
	 */
	$scope.goLogin = function() {
		$rootScope.$broadcast("isLogin",false);
		event.preventDefault();
	};
	/**
	 * 获取优惠后的应付金额
	 */
	$scope.getPayMoney = function(sellerId) {
		// 用户输入的金额  将元转换为分
		var amount = $scope.amount * 100;
		// 用户登录 获取商家的活动信息
		if($scope.isLogin) {
			var card = $scope.card;
			// 获取用户输入的金额
			if(card != undefined && card != null) {
				// 输入金额符合满减金额 则减去相应的折扣
				if(card.isUse == 0 && amount >= card.sums) {					
					if((amount - card.balance)>0){
						amount = amount - card.balance;
					}else{
						amount = 0;
					}
				}
			}
		}
		if(amount < 0) {
			amount = 0;
		}
		$scope.payAmount = amount.toFixed(2);
		var len = $scope.amount.length;
		
		
		var firstNumber = $scope.amount.substring(0,1),
			lastNumber = $scope.amount.substring(len-1,len),
			lastSecondNumber = $scope.amount.substring(len-2,len-1);
		/*console.log("$scope.amount:"+$scope.amount);
		console.log("lastNumber:"+lastNumber);
		console.log("lastSecondNumber:"+lastSecondNumber);*/
		
		if(len===1){
			if($scope.amount==="0"||$scope.amount==="."){
				$scope.amount = "0.00";
			}
		}else if(len===2){
			if(firstNumber==="."&&lastNumber==="0"){
				$scope.amount = "0.00"
			}else if(firstNumber==="."&&lastNumber!=="0"){
				$scope.amount = "0." + lastNumber + "0";
			}else if(firstNumber!=="."&&lastNumber==="."){
				$scope.amount += "00";
			}
		}else if(len>2){
			if(lastNumber==="."&&lastSecondNumber!=="."){
				$scope.amount += "00";
			}
			if(lastNumber!=="."&&lastSecondNumber==="."){
				$scope.amount += "0";
			}
		}			
		//隐藏数字键盘
		$scope.showNumberBoarad=false;
		
		//判断是否已经输入，如果输入了则显示去支付的按钮，否则不显示
		if($scope.amount.length>0){
			$scope.changeInput=true;			
		}else{
			$scope.changeInput=false;
		}
	}
		
}]);
/**
 * 支付成功回调
 */
ctrl.controller('scanPaySuccessCtrl',['$scope','$routeParams','userService',
function($scope,$routeParams,userService){
	userService.scanPaySuccess($routeParams.orderId).success(function(data) {
		$scope.order = data.body.order;
		$scope.sellerName = data.body.sellerName;
		//console.log(data.body);
	}); 
	/**
	 * 继续扫码支付
	 */
	$scope.gotoPay = function(sid) {
		// 跳转到去付款的页面
		window.location.href = ctx+ "v3/scan_code/" + sid;
	}
}]);
