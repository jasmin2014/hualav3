'use strict';
/* App Module */
var app = angular.module('app', [ 'ngRoute','ngTouch', 'ctrl', 'services', 'filters']);
app.config([ '$routeProvider', '$httpProvider','$locationProvider',
		function($routeProvider, $httpProvider/*, $locationProvider*/) {
			var base = ctx + 'html/v3/';
			$routeProvider.when('/login', {
				templateUrl : base + 'comm/detail.html',
				controller : base + 'goodsDetailCtrl'
			}).when('/home', {
				templateUrl : base + 'seller/home.html',
				controller : 'homeCtrl',
				title:'周边看看'
			})
			// 店铺路由
			.when('/seller/:id', {
				templateUrl : base + 'seller/seller.html',
				controller : 'sellerCtrl',
				title:'店铺首页'
			})
			// 店铺路由
			.when('/seller', {
				templateUrl : base + 'seller/seller.html',
				controller : 'sellerCtrl',
				title:'店铺首页'
			})
			//活动页
			.when('/active',{
				templateUrl : base + 'actives/index.html',
				controller : 'sellerCtrl',
				title: '活动页'
			})
			//活动列表页
			.when('/goods/activelist',{
				templateUrl : base + 'actives/activelist.html',
				controller : 'goodsCatCtrl',
				title: '活动列表页'
			})
			//配送范围		
			.when('/seller/:id/scope', {
				templateUrl : base + 'seller/scope.html',
				controller : 'scopeCtrl',
				title:'配送范围'
			})
			// 店铺详情
			.when('/seller/detail/:id', {
				templateUrl : base + 'seller/detail.html',
				controller : 'sellerDetailCtrl',
				title:'店铺详情'
			})
			// 商品分类页面路由
			.when('/goods/cat', {
				templateUrl : base + 'goods/category.html',
				controller : 'goodsCatCtrl',
				title:'商品分类'
			})
			//搜索商品
			.when('/goods/cat/:id/search',{
				templateUrl : base + 'goods/search.html',
				controller : 'searchCtrl',
				title:'商品搜索'
			})
			// 商品详情页面
			.when('/goods/detail', {
				templateUrl : base + 'goods/detail.html',
				controller : 'goodsDetailCtrl as dCtrl',
				title:'商品详情'
			})
			// 购物车
			.when('/cart', {
				templateUrl : base + 'orders/cart.html',
				controller : 'cartCtrl',
				auth:true,
				title:'购物车'
			})
			// 订单处理
			.when('/order/list', {
				templateUrl : base + 'orders/order.html',
				controller : 'orderCtrl',
				auth:true,
				title:'订单列表'
			})
			// 订单详情
			.when('/order/detail/:oid', {
				templateUrl : base + 'orders/detail.html',
				controller : 'orderDetailCtrl',
				auth:true,
				title:'订单详情'
			})
			//"我的"-用户中心页面
			.when('/my',{
				templateUrl : base + 'my/mycenter.html',
				controller : 'myCtrl',
				auth : true,
				title : '个人中心'
			})
			//地址編輯
			.when('/address/edit/:id',{
				templateUrl : base + 'address/address.edit.html',
				controller : 'addressEditCtrl',
				auth : true,
				title : '地址编辑'
			})
			//地址列表
			.when('/address/list/:type', {
				templateUrl : base + 'address/address.list.html',
				controller : 'addressListCtrl',
				auth:true,
				title:'地址列表'
			})
			//“我的”-优惠券页面
			.when('/my/coupon',{
				templateUrl : base + 'my/coupons.html',
				controller : 'myCouponCtrl',
				auth : true,
				title : '个人中心'
			})			
			//“我的”-收藏页
			.when('/my/collections',{
				templateUrl : base + 'my/collections.html',
				controller : 'mycollectionCtrl',
				auth : true,
				title : '收藏页'
			})
			//“我的”-意见反馈页
			.when('/my/feedback',{
				templateUrl : base + 'my/feedback.html',
				controller : 'myfeedbackCtrl',
				auth : true,
				title : '反馈'
			})
			//确认下单			
			.when('/order/confirm/:sellerId', {
				templateUrl : base + 'orders/corder.html',
				controller : 'corderCtrl',
				auth:true,
				title:'确认下单'
			})
			//扫码支付			
			.when('/scan_pay/:sellerId', {
				templateUrl : base + 'pay/pay.html',
				controller : 'scanPayMentCtrl',
				// auth:true,
				title:'扫码支付'
			})
			//支付成功		
			.when('/scan_pay_success/:orderId', {
				templateUrl : base + 'pay/paysuccess.html',
				controller : 'scanPaySuccessCtrl',
				// auth:true,
				title:'支付成功'
			})
			//其它
			.otherwise({
				redirectTo : '/home'
			});
			/*$locationProvider.html5Mode(true);*/
			$httpProvider.interceptors.push('MsgInterceptor');
			$httpProvider.interceptors.push('TokenInterceptor');
		} ]);
/**
 * 路由进入事件
 */
app.run(['$rootScope', '$location','$document','authService',function ($rootScope, $location,$document,authService) {
	//$rootScope.title="花啦生活";
	// Redirect to login if route requires auth and you're not logged in
    $rootScope.$on('$routeChangeStart', function (event, next) {
    	//console.log(event);
    	//console.log(next);
    	//console.log($location);
    	//判断登录
    	if(next.$$route!=undefined&&next.$$route.auth&&!authService.isLogin()){
    		$rootScope.$broadcast("isLogin",false);
    		event.preventDefault();
    	}
    });
    $rootScope.$on('$routeChangeSuccess', function (event, next) {
    	//console.log(event);
    	//console.log(next);
    	//console.log($document);
    	if(next.$$route!=undefined){
    		var t=next.$$route.title;
    		$document[0].title=t==undefined?"花啦生活":t;
    	}
    	//判断登录
    	//$rootScope.$broadcast("title",next.$$route.title);
    });
  }]);
