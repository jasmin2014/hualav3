var MOBILE_REGEX = /^(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/;
app.directive('mobile', function () {
    return {
    	require : 'ngModel',
        restrict: 'AE',
        link: function (scope, element, attrs, ctrl) {
            ctrl.$parsers.unshift(function (viewValue) {
                if (MOBILE_REGEX.test(viewValue)) {
                    ctrl.$setValidity('mobile', true);
                    //console.log(viewValue);
                    return viewValue;
                } else {
                    ctrl.$setValidity('mobile', false);
                    return undefined;
                }
            })
        }
    }
})
/**
 * 店铺收藏
 */
.directive('collect',['$http','userService', function ($http,userService) {
    return {
        replace: true,
        templateUrl:  ctx + 'html/v3/seller/tpl/collect.tpl.html',
        scope: {
        	sellerId:"=",
        	show:"="
        },
        link: function (scope, element, attrs) {
        	/**
        	 *  收藏与取消
        	 */
        	scope.collects = function() {
        		userService.postcollect(scope.sellerId).success(function(d) {
        				//console.log(d);
    					scope.show=(d.success&&d.body>0);
    					if(d.message!=undefined){
    						scope.code=true;
    						scope.message=d.message;
    					}
    			});
    		}
        	/**
        	 * 
        	 */
        	scope.close=function(){
        		scope.code=false;
        	}
        }
    }
}])
