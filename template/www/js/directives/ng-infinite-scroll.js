
var util={

		/**
		 * 获取滚动的高度：
		 * @returns
		 */
		getScrollTop : function() {
			var scrollTop = 0, bodyScrollTop = 0, documentScrollTop = 0;
			if (document.body) {
				bodyScrollTop = document.body.scrollTop;
			}
			if (document.documentElement) {
				documentScrollTop = document.documentElement.scrollTop;
			}
			scrollTop = (bodyScrollTop - documentScrollTop > 0) ? bodyScrollTop
					: documentScrollTop;
			return scrollTop;
		},
		/**
		 * 文档的总高度
		 * @returns
		 */
		getScrollHeight : function() {
			var scrollHeight = 0, bodyScrollHeight = 0, documentScrollHeight = 0;
			if (document.body) {
				bodyScrollHeight = document.body.scrollHeight;
			}
			if (document.documentElement) {
				documentScrollHeight = document.documentElement.scrollHeight;
			}
			scrollHeight = (bodyScrollHeight - documentScrollHeight > 0) ? bodyScrollHeight
					: documentScrollHeight;
			return scrollHeight;
		},
		/**
		 * 可视窗口的高度：可理解为屏幕的像素高度
		 */
		getWindowHeight:function(){
			var windowHeight = 0;
			if(document.compatMode == "CSS1Compat"){
				windowHeight = document.documentElement.clientHeight;
			}else{
				windowHeight = document.body.clientHeight;
			}
			return windowHeight;
		}	
}
app.directive('infiniteScroll', [
  '$rootScope', '$window', '$timeout', function($rootScope, $window, $timeout) {
    return {
      link: function(scope, elem, attrs) {
        var checkWhenEnabled, handler, scrollDistance, scrollEnabled;
        $window = angular.element($window);
        scrollDistance = 0;
        if (attrs.infiniteScrollDistance != null) {
          scope.$watch(attrs.infiniteScrollDistance, function(value) {
            return scrollDistance = parseInt(value, 10);
          });
        }
        scrollEnabled = true;
        checkWhenEnabled = false;
        if (attrs.infiniteScrollDisabled != null) {
          scope.$watch(attrs.infiniteScrollDisabled, function(value) {
            scrollEnabled = !value;
            if (scrollEnabled && checkWhenEnabled) {
              checkWhenEnabled = false;
              return handler();
            }
          });
        };   
        handler = function() {
          var elementBottom, remaining, shouldScroll, windowBottom;
          windowBottom = util.getScrollTop() + util.getWindowHeight();
          elementBottom = elem[0].offsetTop + elem[0].offsetHeight;
          remaining = elementBottom - windowBottom;
          shouldScroll = remaining <= $window[0].innerHeight * scrollDistance;
          if (shouldScroll && scrollEnabled) {
            if ($rootScope.$$phase) {
              return scope.$eval(attrs.infiniteScroll);
            } else {
              return scope.$apply(attrs.infiniteScroll);
            }
          } else if (shouldScroll) {
            return checkWhenEnabled = true;
          }
        };
        $window.on('scroll', handler);
        scope.$on('$destroy', function() {
          return $window.off('scroll', handler);
        });
        return $timeout((function() {
          if (attrs.infiniteScrollImmediateCheck) {
            if (scope.$eval(attrs.infiniteScrollImmediateCheck)) {
              return handler();
            }
          } else {
            return handler();
          }
        }), 0);
      }
    };
  }
]);
