/**
 * 定位，将定位地址显示到页面，如果cookice中有数据，则直接显示
 */
app.directive('hlHeader', ['$cookieStore', '$routeParams', 'geoService', 'cartService', '$location',
    function ($cookieStore, $routeParams, geoService, cartService, $location, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            // transclude: true,
            templateUrl: ctx + "html/v3/comm/header.tpl.html",
            scope: {
                seearound: '=',
                searchbox: '=',
                returnback: '=',
                middle: '=',
                type: "@",
                action: "&"
            },
            link: function (scope, element, attrs) {
                cartService.getList().then(function (c) {
                    if (c.body.length > 0) {
                        scope.cartIcon = true;
                    }
                });
                //购物车小红点事件，
                scope.$on("cartIcon", function (event, msg) {
                    scope.cartIcon = msg;
                    //console.log(msg);
                })
                var gps = $cookieStore.get("gps");
                if (scope.middle != 'hidden') {
                    if (gps == undefined || gps.address == undefined) {
                        scope.geo = "定位中...";
                        new BMap.Geolocation().getCurrentPosition(function(r){
        					if(this.getStatus() == BMAP_STATUS_SUCCESS){
        						var myGeo = new BMap.Geocoder();      
        						geoService.getGeo(r.point.lng,r.point.lat).then(function (re) {
		                            gps = re;
		                            $cookieStore.put("gps", gps);
		                            scope.geo = gps.address;
		                            console.log(scope.action());
		                            scope.action();
		                        })
        					}
        					else {
        						alert('failed'+this.getStatus());
        					}        
        				},{enableHighAccuracy: true})
                    } else {
                        scope.geo = gps.address;
                        console.log(scope.action());
                        scope.action();
                    }
                }
                /**
                 * 点击搜索框时，跳入到搜索页面
                 */
                //console.log(gps);
                scope.gotoSearch = function () {
                    $location.path('/goods/cat/' + gps.sellerId + '/search');
                }
                scope.closemess = function () {
                    scope.Error = false;
                }
            }
        }
    }]);

/**
 * swipe轮播指令
 */
app.directive('hlSwipe', [function () {
    var linker = function (scope, element, attrs) {
        if (scope.$last === true) {
            var e = element.parent().parent();
            var ele = angular.element(element);
            window.mySwipe = new Swipe(e[0], {
                auto: 3000,
                continuous: true,
                callback: function (index, elem) {
                    var on = angular.element(e.children()[1]);
                    var img = e.children()[0];
                    on = on.children();
                    var len = on.length;
                    if (len == 2 && index > len - 1) {
                        img.children[index].innerHTML = img.children[index % len].innerHTML;
                    }
                    on.removeClass("on");
                    angular.element(on[index % len]).addClass("on");
                }
            });
        }
    };
    return {
        link: linker,
        priority: 0
    }

}]);

/**
 *公用的蒙层提醒
 */
app.directive('showmessage', ['$timeout',
    function ($timeout) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: ctx + "html/v3/comm/showmessage.tpl.html",
            scope: {
                time: "@",
                text: "@"
            },
            link: function (scope, elem, attrs) {
                angular.element(document.getElementsByTagName('body')).addClass('i-notscroll');
                //关闭弹出窗口
                scope.closemessageFn = function () {
                    angular.element(document.getElementsByTagName('body')).removeClass('i-notscroll');
                    elem.remove();
                }
                //过期时间不为空，则关闭窗口
                if (scope.time != undefined) {
                    var timer = $timeout(function () {
                        scope.closemessageFn();
                        $timeout.cancel(timer);
                    }, scope.time);
                }
            }
        }
    }
]);

/**
 *用户协议
 */
app.directive('aggrement', ['$http', '$compile',
    function ($http, $compile) {
        return {
            scope: {},
            replace: true,
            templateUrl: ctx + "html/v3/my/tpl/aggrement.tpl.html",
            link: function (scope, elem) {
                scope.addAggrement = function () {
                    $http.get(ctx + "v3/context/aggrement").success(function (data) {
                        angular.element(document.getElementById("aggrement")).html(data.body)
                        scope.show = true;
                    })
                };
                scope.close = function () {
                    scope.show = undefined;
                }
            }
        }
    }
]);
app.directive('squarePicture', [function () {
    return {
        link: function (scope, elem, attrs) {
            var _width = angular.element(elem)[0].width;
            var _height = angular.element(elem)[0].height;
            angular.element(elem)[0].height = _width;
        }
    }
}]);
app.directive('autoFocusWhen', ['$log', '$timeout', function ($log, $timeout) {
    return {
        restrict: 'A',
        scope: {
            autoFocusWhen: '='
        },
        link: function (scope, elem) {
            scope.$watch('autoFocusWhen', function (newValue) {
                if (newValue) {
                    $timeout(function () {
                        elem[0].focus();
                    })
                }
            });
            elem.on('blur', function () {
                scope.$apply(function () {
                    scope.autoFocusWhen = false;
                })
            })
        }
    }
}])