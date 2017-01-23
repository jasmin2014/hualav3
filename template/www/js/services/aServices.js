var services=angular.module('services', []);
services.factory('TokenInterceptor',['$q', '$document', '$location','$rootScope', function ($q,$document, $location,$rootScope) {
    return {
        request: function (config) {
        	//console.log(config);
            config.headers = config.headers || {};
            //if ($window.sessionStorage.token) {
            //    config.headers.Authorization = 'Bearer ' + $window.sessionStorage.token;
            //}
            return config;
        },

        requestError: function(rejection) {
            return $q.reject(rejection);
        },

        /* Set Authentication.isAuthenticated to true if 200 received */
        response: function (response) {
        	//console.log(response);
            //if (response != null && response.status == 200 && $window.sessionStorage.token && !AuthenticationService.isAuthenticated) {
                //AuthenticationService.isAuthenticated = true;
            //}
            return response || $q.when(response);
        },

        /* Revoke client authentication if 401 is received */
        responseError: function(rejection) {
        	//console.log(rejection);
            if (rejection != null && rejection.status === 401) {
            	//console.log($location.absUrl);
            	//console.log($document[0].body);
            	$rootScope.$broadcast("isLogin","fasle");
            }
            return $q.reject(rejection);
        }
    };
}]);
//全局进度展现
services.factory('MsgInterceptor',['$q', '$document', '$location','$rootScope', function ($q,$document, $location,$rootScope) {
    return {
        request: function (config) {
        	$rootScope.loading=true;
            return config;
        },

        requestError: function(rejection) {
        	$rootScope.loading=false;
            return $q.reject(rejection);
        },

        response: function (response) {
        	$rootScope.loading=false;
            return response || $q.when(response);
        },

        responseError: function(rejection) {
        	//console.log(rejection);
        	$rootScope.loading=false;
            return $q.reject(rejection);
        }
    };
}]);



/**
 * 定位服务
 */
services.service('geoService', ['$http','$q','$cookies', function($http,$q,$cookies) {
	/**
	 * 定位通过百度找到最近的地址
	 * { lat:120.2222,
	 *   lng:30.1111,
	 *   adress:"杭州滨江江汉路"
	 * }
	 */
   this.getGeo=function(lng,lat){
            var def = $q.defer();
            $http.jsonp('//api.map.baidu.com/geocoder/v2/?ak=zHNmFc3z0ho52KdH0cH9mDF5LRiulbx6&callback=?&location='+lat+','+lng+'&output=json&pois=0&callback=JSON_CALLBACK').success(function(result) {
         	   var gps={
						lat:result.result.location.lat,
						lng:result.result.location.lng,
						address:result.result.sematic_description
					}   
         	   def.resolve(gps);
              })
          return def.promise;
   }

}]);

/**
 * 定位服务
 */
services.service('wxService', [ function() {
	/**
	 * 微信分享
	 */
   this.menuShare=function(title,link,imgUrl,desc){
	   var shareData ={
		   	      title: title,
		   	      link: link,
		   	      imgUrl:imgUrl,
		   	      desc:desc,
		   	      trigger: function (res) {
		   	        // 不要尝试在trigger中使用ajax异步请求修改本次分享的内容，因为客户端分享操作是一个同步操作，这时候使用ajax的回包会还没有返回
		   	        //alert('用户点击分享到朋友圈');
		   	      },
		   	      success: function (res) {
		   	        //alert('已分享');
		   	      },
		   	      cancel: function (res) {
		   	        //alert('已取消');
		   	      },
		   	      fail: function (res) {
		   	        //alert(JSON.stringify(res));
		   	      }
		   	    };
	 	 wx.config(wxconfig);
	 	 wx.ready(function () {
	 		 wx.onMenuShareTimeline(shareData);
	 		 wx.onMenuShareAppMessage(shareData);
	 	 });
   }

}]);