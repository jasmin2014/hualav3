'use strict';

/* Filters */

var filters = angular.module('filters', []);
filters.filter('checkmark', function() {
	return function(input) {
		return input ? '\u2713' : '\u2718';
	};
});
/**
 * 数据取整
 */
filters.filter("tofixd", function() {
	return function(input) {
		if(input==null){
			return;
		}
		return input.toFixed(1);
	}
});
/**
 * 日期格式化
 */
filters.filter("dateformat", function() {
	return function(input) {
		if(input==null){
			return;
		}
		//return input+'1111'
		input = input.substring(0,19);    
		input = input.replace(/-/g,'/'); 
		var timestamp = new Date(input).getTime();
		return timestamp;
	}
});