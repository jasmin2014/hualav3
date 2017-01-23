/**
 * 登陆控制<br>
 * 登录后将token放置在cookie中，前端判断token如果有，则登录成功，否则不成功
 */
services.service('authService', [ '$http','$cookieStore',
		function($http,$cookieStore ) {
			/**
			 * 登录接口
			 */
			this.login = function(phone, code) {
				var gps=$cookieStore.get("gps");
				return $http.post(ctx + "loginv", {
					phone : phone,
					code : code,
					sellerId:gps==undefined?undefined: gps.sellerId
				});
			}
			/**
			 * 退出接口
			 */
			this.logout = function() {
				return $http.post(ctx + "logout");
			}
			/**
			 * 发送验证码
			 */
			this.sendCode=function(phone){
				return $http.post(ctx + "send?phone="+phone, {
					phone : phone
				});
			};
			/**
			 * 用token来判断是否登录
			 */
			this.isLogin=function(){
				var token=$cookieStore.get("USERID");
				if(token!=undefined){
					return true;
				}else{
					return false;
				}
			}
		} ]);