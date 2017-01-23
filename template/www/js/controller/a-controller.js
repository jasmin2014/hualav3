var ctrl = angular.module('ctrl', [ 'ngCookies', 'services' ]);
ctrl.controller("baseCtrl", [
		'$scope',
		'authService',
		'$templateRequest',
		'$compile','$interval','$route','$cookies',
		function($scope, authService, $templateRequest,
				$compile,$interval,$route,$cookies) {
			//$cookies.put('gps','{"lng":120.208442858,"address":"浙江省杭州市滨江区江汉路","sellerId":568,"lat":30.213219257636}');
			$scope.imgUrl=imgUrl;
			$scope.dotscroll = false;
			var timer ;
			$scope.sendClass="发送验证码";
			/**
			 * 移除登录节点(lgoinForm为登录窗口的ID值，将该节点从页面中移除)
			 */
			$scope.remove = function(loginForm) {
				$scope.code = $scope.phone = undefined;
				angular.element(document.getElementById(loginForm)).remove();
			};
			/**
			 * 登录
			 */
			$scope.login = function() {
				authService.login($scope.phone, $scope.code).success(
						function(data) {
							if (data.success) {
								$scope.remove('loginForm');
								$scope.isLogin = true;
								$route.reload();
							}else{
								alert(data.message);
							}
						});
			};
			/**
			 * 退出登录
			 */
			$scope.logout = function(lgoinForm) {
				authService.logout().success(function(data) {
					if (data.success) {
					}
				});
			};
			/**
			 * 发送验证码
			 */
			$scope.sendCode = function() {
				//console.log($scope.logForm);
				if ( angular.isDefined(timer) ) return;
				$scope.second = 60;
				$scope.sendClass='60s';
				authService.sendCode($scope.phone).success(function(data) {
					console.log(data);
					if (data.success) {
						timer=$interval(function(d) {
							$scope.sendClass=($scope.second--)+"s";
							 if($scope.second<=0){
								 $scope.sendClass="发送验证码";
						         $interval.cancel(timer);
						         timer=undefined;
							 }
						},1000) 
					}else{
						$scope.sendClass="发送验证码";
						$scope.errormessage = data.message;
					}
				});
			};
			
			/**
			 * 登录窗口弹出
			 */
			$scope.$on("isLogin", function(event, isLogin) {
				$templateRequest(ctx + "/html/v3/comm/login.tpl.html").then(
						function(value) {
							angular.element(document.getElementById("ng-view"))
									.append($compile(value)($scope));
						});
			})
		} ]);
