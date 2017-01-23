/**
 * 首页周边看看控制器
 */
ctrl.controller('goodsCatCtrl', [
		'$scope',
		'$http',
		'$location',
		'$routeParams',
		'cartService','$anchorScroll','$cookieStore','wxService',
		function($scope, $http, $location, $routeParams, cartService,$anchorScroll,$cookieStore,wxService) {
			$scope.cartList = cartService.getList();
			//默认数据没展现前不出现blank页面
			$scope.loadingdata = false;
			/**
			 * 获取分类列表
			 */
			//console.log('sellerId:'+$routeParams.sellerId);
			//console.log('supplierId:'+$routeParams.supplierId);
			var gps = $cookieStore.get("gps");
			
			var sellerId=$routeParams.sellerId==undefined?gps.sellerId:$routeParams.sellerId;
			$http.get(ctx + "/v3/goods/" + sellerId + "/cat").success(
					function(d) {
						$scope.cates = d.body.cat;
						$scope.subcatename = $scope.cates[0].cname;
						// 初始化类别名称
						$scope.supplier = d.body.supplier;
						// 初始化子类
						$scope.subcates = $scope.cates[0].subCatList;
						$scope.active = $scope.cates[0].cid;
					});
			/**
			 * 类目显示控制 false不显示
			 */
			$scope.sellerId = sellerId;
			$scope.sellerName = $routeParams.sellerName;
			$scope.address = gps.address;
			//console.log($scope.address);

			/**
			 * 查询子类目初始化变量
			 */
			$scope.init = function(page, cid, cname, type) {
				$scope.myVar = false;
				$scope.show = false;
				$scope.show2 = false;				
				/**
				 * 类型为1，则为父类目
				 */
				$scope.type = type;
				/**
				 * 是否数据加载完
				 */
				$scope.finish = false;
				/**
				 * 显示的类目名称
				 */
				if (cname != undefined) {
					$scope.subCateName = cname;
				}
				/**
				 * 分页信息
				 */
				$scope.page = page;
				/**
				 * 类目id
				 */
				if (cid != undefined) {
					$scope.cid = cid;
				}
				/**
				 * 商品数据列表
				 */
				$scope.goodsList = [];
				
				$scope.currentActiveGoods = [];
			}
			// 获取商品列表
			/**
			 * 商品数据列表
			 * 
			 * @param cid
			 *            分类ID
			 * @param cname
			 *            分类名称
			 */

			$scope.getGoodsList = function() {
				if ($scope.finish)
					return;
				var url = ctx + "v3/goods/" + sellerId + "/list";
				var param = {
					params : {
						"page" : $scope.page,
						"size" : 27,
						"cateId" : $scope.cid,
						"type" : $scope.type,
						"sort" : $scope.sort,
						"supplierId" : $scope.supplierId
					}
				};

				// 购物车与商品数据结合处理购物车数量
				$scope.cartList.then(function(c) {
					$http.get(url, param).success(
					function(d) {
						if (d.body.length < param.params.size) {
							$scope.finish = true;
						}
						if(d.body.length==0){
							//数据接口调用成功后如果没有数据则显示blank页面
							$scope.loadingdata = true;
						}
						d.body.forEach(function(e) {
							e.goodNum = cartService.getCart(c.body,$routeParams.sellerId, e.skuId);
							if(e.actImgUrl!==undefined){
								$scope.currentActiveGoods.push(e);
							}else{
								$scope.goodsList.push(e);
							}
						});
						$scope.page++;							
					})
				})
			};
			/**
			 * 点击子节点获取商品列表
			 * 
			 * @param type为1是标记为父类目
			 */
			$scope.getGoods = function() {
				$scope.getGoodsList();
			}
			
			/**
			 * 获取小分类
			 */
			$scope.subCat = function(cat) {
				$scope.subcates = cat.subCatList;
				$scope.active = cat.cid;
				$scope.subcatename = cat.cname;
			};
			/**
			 * 获取指定分类商品
			 */
			$scope.getCat = function(cid, cname, type) {
				$scope.init(1, cid, cname, type);
				//$scope.getGoods();
				$scope.getGoodsList();
			}
			/**
			 * 获取指定供应供应商商品
			 */
			$scope.getSup = function(supplierId) {
				$scope.supplierId = supplierId;
				$scope.init(1, $scope.cid, $scope.cname, $scope.type);
				//$scope.getGoods();
				$scope.getGoodsList();
			}
			/**
			 * 点击排序处理 key排序字段
			 */
			$scope.getSort = function(key) {
				$scope.sort = key;
				$scope.init(1, $scope.cid, $scope.cname, $scope.type);
				//$scope.getGoods();
				$scope.getGoodsList();
			}
			// type标记如果是1，则标记为父类目 初始化方法
			if($routeParams.supplierId&&$routeParams.supplierId!=''){
				$scope.getSup($routeParams.supplierId);
				$routeParams.cname="全部分类"
			}else{
				$scope.getCat($routeParams.cid,
						$routeParams.cname == undefined ? "全部分类"
								: $routeParams.cname, $routeParams.type);
			}
				
			
			
			 //回到顶部
			$scope.gotop = function(){
			      $anchorScroll('top');
			 }
			if($routeParams.actImg&&$routeParams.actImg!=''){
				$scope.actImg = $routeParams.actImg;
				//console.log($scope.actImg);
			}
			var url = $location.protocol()+"://"+location.host+ctx+"goshopgoods/"+$scope.sellerId;
			wxService.menuShare($scope.sellerName, url,$location.protocol()+"://"+location.host+ctx+"assets/v3/images/huala-logo.jpg",$scope.sellerName+"店的商品列表");  
		} ]);
/**
 * 商品搜索页控制器
 */
ctrl.controller('searchCtrl', [
		'$scope',
		'$http',
		'$routeParams',
		'cartService','$cookieStore',
		function($scope, $http, $routeParams, cartService, $cookieStore) {
			var gps = $cookieStore.get("gps");
			var sellerId=$routeParams.id==undefined?gps.sellerId:$routeParams.id;
			
			$scope.sellerId = sellerId;
			
			$scope.searchFocus = true;
			/**
			 * 获取购物车数据
			 */
			$scope.cartList = cartService.getList();
			/**
			 * 初始化参数
			 */
			$scope.init = function(page, finish) {
				$scope.page = page;
				$scope.finish = finish;
			}
			/**
			 * 搜索商品方法searchItems()
			 */
			$scope.init(1);
			$scope.goodsList = [];
			$scope.searchItems = function(key) {
				$scope.key=key;
				if ($scope.finish)
					return;
				var url = ctx + "v3/goods/" + sellerId + "/search";
				var param = {
					params : {
						"key" : key,
						"page" : $scope.page,
						"size" : 27
					}
				};
				$scope.cartList.then(function(c) {
					$http.get(url, param).success(
					function(d) {
						if (d.body.length == 0) {
							$scope.finish = true;
						}
						d.body.forEach(function(e) {
							e.goodNum = cartService.getCart(c.body,sellerId, e.skuId);
							$scope.goodsList.push(e);
						});
						$scope.page++;
					})
				})
			};
			/**
			 * 点击“大家都在搜”下面的关键词时显示搜索出来的结果
			 */
			$scope.searchMyItems = function(key) {
				// 设置新搜索的初始化页码为1
				$scope.init(1);
				// 清除上一次搜索出来的结果，将新的结果重新追加到goodsList上显示出来
				$scope.goodsList = [];
				$scope.searchItems(key);
			}
			/**
			 * 点击enter键时调用搜索方法
			 */
			$scope.mykeyUp = function(e) {
				var keycode = window.event ? e.keyCode : e.which;
				if ($scope.key === '')
					return;
				if (keycode == 13) {
					$scope.init(1);
					$scope.goodsList = [];
					$scope.searchItems($scope.key);
				}
			}
		} ])

/**
 * 商品详情页控制器
 */
ctrl.controller('goodsDetailCtrl', [
		'$scope',
		'$http',
		'$location',
		'$routeParams',
		'$sce',
		'$anchorScroll',
		'wxService',
		function($scope, $http, $location, $routeParams,$sce,$anchorScroll,wxService) {
			var self = this;		
			$scope.getGraphic = function(){
				if($scope.GoodsDetail&&$scope.GoodsDetail!=''){
					self.explicitlyTrustedHtml = $sce.trustAsHtml($scope.GoodsDetail);
					// 将location.hash的值设置为
			        // 你想要滚动到的元素的id
			        //$location.hash('graphic-details');
			        // 调用 $anchorScroll()
			        $anchorScroll('graphic-details');
					
				}
				
			}
			/**
			 * 获取商品详细信息
			 */
			var shareImg = '';
			$http.get(ctx + "/v3/goods/" + $routeParams.sellerId + "/"+ $routeParams.goodsId).success(function(d) {
				$scope.goods = d.body.goods;
				$scope.seller = d.body.seller;
				if($scope.goods.goodsDetail&&$scope.goods.goodsDetail!=''){
					$scope.GoodsDetail = $scope.goods.goodsDetail;
					$scope.showGoodsDetail = true;
				}else{
					$scope.showGoodsDetail = false;
					$scope.GoodsDetail = undefined;
				}
				if($scope.goods.picUrl != undefined && $scope.goods.picUrl != ""){
					shareImg = imgUrl+$scope.goods.picUrl+'?s=220x220';
				}else{
					shareImg = $location.protocol()+"://"+location.host+ctx+"assets/v3/images/huala-logo.jpg"
				}
				var url = $location.protocol()+"://"+location.host+ctx+"goshopgoodsdetail/"+$scope.seller.id+"/"+$scope.goods.id;
				wxService.menuShare($scope.goods.title, url, shareImg ,$scope.seller.address+"　"+$scope.seller.name);
			});
			
			/**
			 * 判断店铺是否已收藏
			 */
			$http.get(ctx + "v3/iscollects/" + $routeParams.sellerId).success(
			function(d) {
				$scope.show = d.success;
			});
			 //回到顶部
			$scope.gotop = function(){
			      $anchorScroll('top');
			 }
			
			
		} ]);
