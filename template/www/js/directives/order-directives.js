
/**
 * 展示产品列表
 */
app.directive('showgoods', [ function() {
	return {
		restrict : 'AE',
		replace : true,
		// transclude: true,
		templateUrl : ctx + 'html/v3/orders/tpl/goodslist.tpl.html',
		link : function(scope, elem, attrs) {
			/**
			 * 关闭窗口
			 */
			scope.closePops=function(){
				elem.remove();
			}
			scope.totalNumber = 0;
			scope.orderGoods=scope.order.orderGoods;
			scope.orderGoods.forEach(function(elt, i) {
				scope.totalNumber = scope.totalNumber
				+ elt.goodsNumber;
			})
		}
	}
} ]);
/**
 * 展示预约时间
 */
app.directive('showtimes', ['$timeout','$compile','orderService',function($timeout,$compile,orderService) {
	return {
		restrict : 'AE',
		replace : true,
		templateUrl : ctx + 'html/v3/orders/tpl/choosedate.tpl.html',
		link : function(scope, elem, attrs) {
			/**
			 * 关闭窗口
			 */
			scope.closePops=function(){
				elem.remove();
			}
			/** first: 获取配送时间数据
			 *  second: $scope.dayname 今天或明天被选中时的初始化值, $scope.timename 时间段被选中的初始化值
			 */
			orderService.getChooseDate(scope.order.sellerId).success(function(data) {
				 var d=data.body;
				 //console.log(d);
				 scope.orderTimes=d;
				 scope.list=d.today.list;
				 //choose表示选择的是哪天
				 scope.choose=d.today.name;
			 })
			 /**
			  * 选择日期：今天或者明天
			  */
			 scope.chooseDay=function(day){
				 scope.list=day.list;
				 scope.choose=day.name;
			 }
			 /**
			  * 选择时间，选中的时间将天与时间显示出来
			  */
			scope.choosedate = function(time) {
				scope.showTime=time.day+" "+time.showTime;
				scope.bestTime=time.bestTime;
			};
			scope.showMess = function(){				
				if(scope.bestTime != undefined){
					//选中时间，则将时间赋值给订单的bestTime
					scope.order.orderSendTime = scope.showTime;
					scope.order.bestTime=scope.bestTime;
					elem.remove();
				}else{
					//没有选中，则弹出提示窗
					elem.append($compile("<showmessage time=2000 text='请选择预约时间'></showmessage>")(scope));
				}
			};
		}
	}
} ]);
/**
 * 优惠券
 */
app.directive('showcoupons', [ function() {
	return {
		restrict : 'AE',
		replace : true,
		templateUrl : ctx + 'html/v3/orders/tpl/choosecoupons.tpl.html',
		link : function(scope, elem, attrs) {
			scope.choose = function(t){
				scope.red=t;
				//console.log(t);
			};
			scope.confirm = function(popId){				
				/**
				 * 关闭弹窗模块Fn
				 */
				//console.log(scope.redFn);
				if(scope.redFn!=undefined){
    			  scope.redFn(scope.red,true);
    		    }
				elem.remove();
			};
			scope.closePops=function(){
				elem.remove();
			}
		}
	}
} ]); 
