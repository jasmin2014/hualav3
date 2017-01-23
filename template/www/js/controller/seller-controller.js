/**
 * 首页周边看看控制器
 */
ctrl.controller('homeCtrl', [ '$scope', '$http','$cookieStore', '$location', 'wxService',
	function($scope, $http,$cookieStore,$location,wxService) {
	    $scope.seller=[];
		$scope.page=0;
		/**
		 * 如果在执行中，有其他请求，则不执行接口数据
		 */
		
		$scope.exe=false;
		/**
		 * 设置默认数据没有出来时
		 */
		$scope.loadingdata = false;
		/**
		 * 是否没有数据，如果没有，则不执行接口请求
		 */
		$scope.noMore=false;
		$scope.getList=function(){
			var gps = $cookieStore.get("gps");
			if(gps){
				if($scope.exe||$scope.noMore){
					return;
				}
				$scope.exe=true;
				$scope.page=$scope.page+1;
				var params={params:{"page":$scope.page,"size":10,"lat":gps.lat,"lng":gps.lng,"address":gps.address}};
				$http.get(ctx + "v3/seller/list",params).success(function(data) {
					$scope.exe=false;
					if(data.success){
						data.body.forEach(function(elt, i) {
							$scope.seller.push(elt);
						});
						if(data.body!=undefined&&data.body.length<10){
							$scope.noMore=true;
						};
			     		if(data.body.length==0){
				     		$scope.loadingdata = true;	
			     		}
					}
		     	});
			}
		}
		var url = $location.protocol()+"://"+location.host+ctx;
		//console.log(url);
		wxService.menuShare("周边看看", url, $location.protocol()+"://"+location.host+ctx+"assets/v3/images/huala-logo.jpg","邀请你去周边看看");    			
	} 
]);
/**
 * 店铺页面控制器
 */
ctrl.controller('sellerCtrl', [ '$scope', '$http',"$location", '$compile', '$routeParams','$cookieStore','wxService',
   function($scope, $http,$location, $compile, $routeParams, $cookieStore, wxService) {
	    /**
	     * 获取店铺与分类信息
	     */		
		$scope.getSeller=function(){		
			var gps = $cookieStore.get("gps");
			var sellerId=$routeParams.id==undefined?gps.sellerId:$routeParams.id;
			$http.get(ctx + "v3/seller",{params:{"sellerId":sellerId,"lat":gps.lat,"lng":gps.lng,"address":gps.address}}).success(function(d) {
				if(d.body.actList&&d.body.actList.length>0){
					$scope.actList = d.body.actList;
				}				
	     		$scope.seller = d.body.seller;
	     		$scope.cat = d.body.cat;
	     		$scope.banners = d.body.banner;
	     		$cookieStore.put("gps", d.body.gps);
	     		/**
    			* 判断店铺是否已收藏
    	 		*/
    			$http.get(ctx + "v3/iscollects/"+$scope.seller.id).success(function(d) {
    				$scope.show=d.success;
    			});
    			/**
    			 * 点击领取红包
    			 */
    			angular.element(document.getElementById('ng-view')).append($compile('<package-blank seller-id="seller.id" seller-name="seller.name"></package-blank>')($scope));
    			var shareImg = '';
    			if($scope.seller.imgUrl != undefined && $scope.seller.imgUrl != ""){
					shareImg = imgUrl+$scope.seller.imgUrl+'?s=220x220';
				}else{
					shareImg = $location.protocol()+"://"+location.host+ctx+"assets/v3/images/huala-logo.jpg"
				}
    			var url = $location.protocol()+"://"+location.host+ctx+"goshop/"+$scope.seller.id;
				wxService.menuShare($scope.seller.name, url,$location.protocol()+"://"+location.host+ctx+"assets/v3/images/huala-logo.jpg",$scope.seller.address);    			
			});
		}		
    } 
]);
/**
 * 店铺详情控制器
 */
ctrl.controller('sellerDetailCtrl', [ '$scope', '$http','$routeParams','$cookieStore',function($scope, $http,$routeParams,$cookieStore) {
	/**
	 * 判断店铺是否已收藏
	 */
	$http.get(ctx + "v3/iscollects/"+$routeParams.id).success(function(d) {
		$scope.show=d.success;
	});
    /**
     * 获取店铺详情的数据
     */  
	$http.get(ctx+"v3/seller/detail/"+$routeParams.id).success(function(data) {
		$scope.seller = data.body;
		$scope.lng = data.body.lng;
		$scope.lat = data.body.lat;
	});
  
} ]);
/**
 * 配送范围
 */
ctrl.controller('scopeCtrl', [ '$scope', '$http', '$routeParams','$cookieStore','$window',function($scope,$http,$routeParams,$cookieStore,$window) {
	// 百度地图API功能
	/**
     * 获取店铺详情的数据
     */  
	$window.init=function(){
    	/**
    	 * sellerId:店铺ID
    	 */
    	var sellerId = $routeParams.id;
    	/**
    	 * myKeys: 配送范围内所包括的关键词
    	 */
    	var myKeys = ["小区", "写字楼", "街道","学校", "加油站"];
    	/**
    	 * map:创建一个地图实例
    	 */
    	var map = new BMap.Map("allmap");            
    	var mPoint = new BMap.Point($routeParams.lng,$routeParams.lat); 
    	var pt = new BMap.Point($routeParams.lng, $routeParams.lat);
    	var myIcon = new BMap.Icon(ctx+"assets/v3/images/localicon.png", new BMap.Size(45,45));
    	var marker = new BMap.Marker(pt,{icon:myIcon});  // 创建标注
    	/**
    	 * 定位gps  lat:  lng:
    	 */
    	var gps = $cookieStore.get('gps');	
    	/**
    	 * 定位成功的情况下，给当前所在的定位用一个蓝色的亮点标记出来
    	 */
    	if(gps!=undefined){
    		var pt1 = new BMap.Point(gps.lng, gps.lat);
    		var myIcon1 = new BMap.Icon(ctx+"assets/v3/images/locationDot.png", new BMap.Size(30,30));
    		var marker1 = new BMap.Marker(pt1,{icon:myIcon1});  // 创建标注
    		map.addOverlay(marker1);
    	}
    	/**
    	 * 点击店铺形状的图标时显示店铺的信息窗
    	 */
    	marker.addEventListener("click", function(){
    		var opts = {
  			  width : 100,     // 信息窗口宽度
  			  height: 10,     // 信息窗口高度
  			  title : data.body.aliasName , // 信息窗口标题
  			  enableMessage:false, //设置允许信息窗发送短息
  			  message:""
  			}
  			var infoWindow = new BMap.InfoWindow(data.body.address, opts);  // 创建信息窗口对象 
  			map.openInfoWindow(infoWindow,mPoint); //开启信息窗口    		
    	});
    	/**
    	 * 左上角，添加比例尺
    	 */
    	var top_left_control = new BMap.ScaleControl({anchor: BMAP_ANCHOR_TOP_LEFT});
    	map.addControl(top_left_control);
    	/**
    	 * 右上角
    	 */
    	var top_right_navigation = new BMap.NavigationControl({anchor: BMAP_ANCHOR_TOP_RIGHT, type: BMAP_NAVIGATION_CONTROL_SMALL}); 
    	map.addControl(top_right_navigation);
    	map.enableScrollWheelZoom();
    	map.centerAndZoom(mPoint,15);
    	/**
    	 * 创建配送范围周围2000的蓝色显示圈
    	 */
    	var circle = new BMap.Circle(mPoint,2000,{fillColor:"blue",fillOpacity: 0.3, strokeWeight:1, strokeOpacity:0.3});
    	map.addOverlay(marker);
    	map.addOverlay(circle);
   }
  //百度地图API功能
    function loadJScript() {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://api.map.baidu.com/api?v=2.0&ak=16005cc9538518f22a3b1c48fe36ea96&callback=init";
        document.body.appendChild(script);
    }
 
    window.onload = loadJScript();  //异步加载地图  
} ]);