/**
 * 点击搜索地址
 */
app.directive('addressSearch', ['$cookieStore', 'userService', function ($cookieStore, userService) {
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            address: "=",
        },
        templateUrl: ctx + 'html/v3/address/address.search.tpl.html',
        link: function (scope, elem, attrs) {
            /**
             * 地址搜索
             */
            scope.search = function () {
                var gps = $cookieStore.get("gps");
                if (gps == undefined)gps = {};
                userService.gpsAddress(scope.key, gps.lat, gps.lng).success(function (data) {
                    scope.addressList = data.body;
                });
            };
            scope.choosed = function (t) {
                if (scope.address == undefined)scope.address = {};
                scope.address.signBuilding = t.name;
                //scope.address.address = t.address;
                scope.address.lat = t.lat;
                scope.address.lng = t.lng;
                scope.removedirective();
            }
            /**
             * 地址切换
             */
            scope.removedirective = function () {
                elem.remove();
            }
            scope.search();

            //默认不显示城市切换列表
            scope.showcities = false;
            //默认地址为“杭州”
            scope.currentCity = '杭州';
            $cookieStore.put("currentCity", "杭州");
            scope.citylist = [{city: "330100", name: '杭州'}, {city: "500000", name: '重庆'}];
            scope.showCitiesFn = function () {
                scope.showcities = true;
            }
            scope.changeCity = function (city) {
                scope.currentCity = city.name;
                $cookieStore.put("currentCity", city.name);
                scope.showcities = false;
            }
        }
    }
}]);
/**
 * 意见反馈-提示
 */
app.directive('showfeedpop', [function () {
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            text: "=",
            nosubmit: "="
        },
        templateUrl: ctx + 'html/v3/my/tpl/feedbackpop.tpl.html',
        link: function (scope, elem, attrs) {
            scope.submitok = function () {
                console.log("提交成功");
            };
            scope.cancelsubmit = function () {
                elem.remove();
            }
        }
    }
}]);
/**
 * 获取红包的弹窗指令
 */
app.directive('packageGet', [function () {
    return {
        restrict: "AE",
        replace: true,
        templateUrl: ctx + "html/v3/seller/tpl/package_get.tpl.html",
        link: function (scope, elem, attrs) {
            scope.remove = function () {
                elem.remove();
            }
        }
    }
}]);
/**
 * 红包为空时的弹窗指令
 */
app.directive('packageBlank', ['$http', '$rootScope', '$location', function ($http, $rootScope, $location) {
    return {
        restrict: "AE",
        replace: true,
        scope: {
            sellerId: "=",
            sellerName: "="
        },
        templateUrl: ctx + "html/v3/seller/tpl/package_blank.tpl.html",
        link: function (scope, elem, attrs) {

            var url = ctx + "v3/homeRedPackage/" + scope.sellerId;
            //console.log(scope.seller);
            $http.get(url).success(function (data) {
                if (data.body != undefined) {
                    scope.show = true;
                    scope.act = data.body;
                    //console.log(scope.act);
                }
            });
            /**
             * 显示红包领取状态
             */
            scope.getpackage = function () {
                var array = new Array();
                scope.act.forEach(function (data) {
                    array.push(data.id);
                });
                $http.post(url, array).success(function (data) {
                    if (data.success) {
                        scope.show = false;
                        scope.hasAct = true;
                    } else {
                        alert(data.message)
                    }
                })
            }
            scope.remove = function () {
                scope.hasAct = false;
            }
            /**
             * 跳转去使用红包相关页面
             */
            scope.gotouseFn = function (supplierid) {
                if (supplierid && supplierid != '') {
                    window.location.href = '#/goods/cat?sellerId=' + scope.sellerId + "&supplierId=" + supplierid;
                    //$location.path('#/goods/cat?sellerId='+scope.sellerId+"&supplierId="+supplierid);
                } else {
                    window.location.href = '#/goods/cat?sellerId=' + scope.sellerId;
                }
            }
        }
    }
}]);
/**
 * 数字键输入指令
 */
app.directive('getNumber', ["$routeParams", function ($routeParams) {
    return {
        link: function (scope, elem, attrs) {
            //console.log($routeParams.sellerId);
            elem.on('touchstart', function () {

                scope.amount += elem.children('span').html();

                var p = /^[^\.]*$/;

                var firstNumber = scope.amount.substring(0, 1),
                    secondNumber = scope.amount.substring(1, 2),
                    thirdNumber = scope.amount.substring(2, 3);

                var dotNumber = scope.amount.indexOf('.'),
                    length = scope.amount.length,
                    lastStr = scope.amount.substring(scope.amount.indexOf('.'), scope.amount.length),
                    numberStr = scope.amount.substring(0, scope.amount.indexOf('.'));

                //如果所输入的第一个数字为“0”并且第二个字符不为空并且不为“.”是，去掉第一个数字0，则：
                if (firstNumber === "0" && secondNumber !== "." && secondNumber !== '') {
                    scope.amount = scope.amount.substring(1, scope.amount.length) + '.00';
                }
                //如果输入的第一个数字为“.”并且第二个数字不是“.”也不是空，第三个数字不是“.”也不是空，则：
                else if (firstNumber === "." && secondNumber !== "." && secondNumber !== '' && thirdNumber !== "." && thirdNumber !== "") {
                    scope.amount = "0" + scope.amount;
                }
                //如果第一个数字为“.”第二个数字也为“.”，则：
                else if (firstNumber === "." && secondNumber === ".") {
                    scope.amount = "0.00";
                } else {
                    if (!p.test(lastStr.substring(1, lastStr.length)) && scope.amount !== "0.00") {
                        lastStr = lastStr.substring(1, lastStr.length).replace(".", "0");
                        //console.log(lastStr);
                        scope.amount = numberStr + "." + lastStr;
                    }
                }

                if (dotNumber != -1 && length > 3) {
                    scope.amount = scope.amount.substring(0, dotNumber + 3);
                } else {
                    scope.amount = scope.amount;
                    numberStr = scope.amount;
                }
                if (!p.test(lastStr.substring(1, lastStr.length)) && scope.amount !== "0.00") {
                    lastStr = lastStr.substring(1, lastStr.length).replace(".", "0");
                    scope.amount = numberStr + "." + lastStr;
                }

                if (parseInt(numberStr) >= 20000) {
                    scope.amount = "20000";
                }

                angular.element(document.getElementById("amount")).val(scope.amount);
                angular.element(document.getElementById("scanGoPay")).removeClass("gotoPay");

                // 用户输入的金额  将元转换为分
                var amount = scope.amount * 100;
                scope.payAmount = amount;
                // 用户登录 获取商家的活动信息
                if (scope.isLogin) {
                    var card = scope.card;
                    // 获取用户输入的金额
                    if (card != undefined && card != null) {
                        // 输入金额符合满减金额 则减去相应的折扣
                        if (card.isUse == 0 && amount >= card.sums) {
                            if ((amount - card.balance) > 0) {
                                scope.payAmount = amount - card.balance;
                            } else {
                                scope.payAmount = 0;
                            }
                        }
                    } else {
                        scope.payAmount = amount;
                    }
                }

                angular.element(document.getElementById("payAmount")).html("￥" + (scope.payAmount / 100).toFixed(2));

            })
        }
    }
}]);
app.directive("deleteNumber", [function () {
    return {
        link: function (scope, elem, attrs) {
            elem.on("touchstart", function () {
                var l = scope.amount.length;
                if (l > 0) {
                    var a = scope.amount.substring(0, l - 1);
                    scope.amount = a;
                    // 用户输入的金额  将元转换为分
                    var amount = scope.amount * 100;
                    scope.payAmount = amount;
                    // 用户登录 获取商家的活动信息
                    if (scope.isLogin) {
                        var card = scope.card;
                        // 获取用户输入的金额
                        if (card != undefined && card != null) {
                            // 输入金额符合满减金额 则减去相应的折扣
                            if (card.isUse == 0 && amount >= card.sums) {
                                if ((amount - card.balance) > 0) {
                                    scope.payAmount = amount - card.balance;
                                } else {
                                    scope.payAmount = 0;
                                }
                            }
                        } else {
                            scope.payAmount = amount;
                        }
                    }
                    angular.element(document.getElementById("amount")).val(a);
                    angular.element(document.getElementById("payAmount")).html("￥" + scope.payAmount / 100);
                    angular.element(document.getElementById("scanGoPay")).removeClass("gotoPay");
                } else {
                    angular.element(document.getElementById("scanGoPay")).addClass("gotoPay");
                }
            })
        }
    }
}])
