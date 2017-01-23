var gulp = require('gulp'),
//css
sass = require('gulp-ruby-sass'),
autoprefixer = require('gulp-autoprefixer'),
minifycss = require('gulp-minify-css'),
//js
jshint = require('gulp-jshint'),
uglify = require('gulp-uglify'),
//img
imagemin = require('gulp-imagemin'),
//html
minifyHtml = require('gulp-minify-html'),
//comm
livereload = require('gulp-livereload'),
cache = require('gulp-cache'),
concat = require('gulp-concat');
rename = require('gulp-rename'),
clean = require('gulp-clean'),
gulpif=require('gulp-if'),
notify = require('gulp-notify');

var src={
	css:"www/css/**/*.*",
	js:"www/js/**/*.*",
	html:"www/views/**/*.html",
    js_lib:["www/lib/angular.js","www/lib/angular-route.js","www/lib/angular-cookies.js","www/lib/angular-touch.js"],
    js_third :"www/lib/third/**/*.*"
}
var dest={
		css:"../src/main/resources/assets/v3/css",
		js:"../src/main/resources/assets/v3/js",
		html:"../src/main/resources/html/v3"
	}
//是否启用压缩
var condition=true;
var modal="开发模式";
//预设任务
gulp.task('default',['clean'], function() {
	modal='生产模式';
	gulp.start('css', 'js','js_lib','html','watch');
	// 将你的默认的任务代码放在这
});
//预设任务
gulp.task('dev',['clean'], function() {
	condition=false;
	gulp.start('css', 'js','js_lib','html','watch');
	// 将你的默认的任务代码放在这
});
//样式
gulp.task('css', function() {  
 ret= gulp.src(src.css);
 //ret=ret.pipe(sass({ style: 'expanded', }));
 //ret=ret.pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'));
 ret=ret.pipe(concat('main.css'));
 //ret=ret.pipe(gulp.dest('dist/css'));
 ret=ret.pipe(rename({ suffix: '.min' }));
 ret=ret.pipe(gulpif(condition,minifycss()));
 ret=ret.pipe(gulp.dest(dest.css));
 ret=ret.pipe(notify({ message: modal+'CSS整理完成' }));
 return ret;
});
//脚本
gulp.task('js_lib', function() {  
	
   /**
    * ANGULARJS库
    */
   ret=gulp.src(src.js_lib);
   ret=ret.pipe(jshint.reporter('default'));
   ret=ret.pipe(concat('lib.js'));
   ret=ret.pipe(rename({ suffix: '.min' }));
   ret=ret.pipe(gulpif(condition,uglify()));
   ret=ret.pipe(gulp.dest(dest.js));
   ret=ret.pipe(notify({ message: modal+'JS_lib整理完成' }));
   
   ret=gulp.src(src.js_third);
   ret=ret.pipe(jshint.reporter('default'));
   ret=ret.pipe(concat('third.js'));
   ret=ret.pipe(rename({ suffix: '.min' }));
   ret=ret.pipe(gulpif(condition,uglify()));
   ret=ret.pipe(gulp.dest(dest.js));
   ret=ret.pipe(notify({ message: modal+'JS_third整理完成' }));
   
   
   
   return ret;
});
//脚本
gulp.task('js', function() {  
   ret=gulp.src(src.js);
   ret=ret.pipe(jshint.reporter('default'));
   ret=ret.pipe(concat('main.js'));
   ret=ret.pipe(rename({ suffix: '.min' }));
   ret=ret.pipe(gulpif(condition,uglify()));
   ret=ret.pipe(gulp.dest(dest.js));
   ret=ret.pipe(notify({ message: modal+'JS整理完成' }));
   return ret;
});


// 压缩Html/更新引入文件版本
gulp.task('html', function() {
	
	ret = gulp.src(src.html);

	// .pipe(revCollector())
	ret = ret.pipe(gulpif(condition, minifyHtml({
		empty : true,
		spare : true,
		quotes : true
	})));
	ret = ret.pipe(gulp.dest(dest.html));
	ret = ret.pipe(notify({ message: modal+'HTML整理完成' }));
	return ret;
});

// gulp.task('html', function() {
//   ret=gulp.src(src.html);
   //ret=ret.pipe(gulp.dest('dist/js'));
   //ret=ret.pipe(rename({ suffix: '.min' }));
   //ret=ret.pipe(uglify());
//   ret=ret.pipe(gulp.dest(dest.html));
//   ret=ret.pipe(notify({ message: 'Html整理完成' }));
//   return ret;
//});
//图片
//gulp.task('images', function() {  
//return gulp.src('src/images/**/*')
// .pipe(cache(imagemin({ optimizationLevel: 3, progressive: true, interlaced: true })))
// .pipe(gulp.dest('dist/images'))
// .pipe(notify({ message: 'Images task complete' }));
//});

//清理
gulp.task('clean', function() {
	return gulp.src([ dest.css, dest.js,dest.html ], {
		read : false
	}).pipe(clean({
		force : true
	}));
});



//看手
gulp.task('watch', function() {

	// 看守所有.scss档
	gulp.watch(src.css, ['css']);
	
	// 看守所有.js档
	gulp.watch(src.js, ['js']);
	gulp.watch(src.html,function(event) {
		console.log('File ' + event.path + ' was ' + event.type + ', running tasks...');
		gulp.start('html');
	});
	//gulp.watch(src.js_lib, ['js_lib']);
	//return gulp.watch(base_src_js+'/js',function(){
	//	console.log("aaa");
	//    gulp.run('js');
	//});
	// 看守所有图片档
	//gulp.watch('src/images/**/*', ['images']);
	
	// 建立即时重整伺服器
	//livereload.listen();
	//// Watch any files in assets/, reload on change
	//gulp.watch([base_src_js+'/**']).on('change', livereload.changed);
	//
	//});
	//var server = livereload();
	//// 看守所有位在 dist/  目录下的档案，一旦有更动，便进行重整
	//gulp.watch([base_dest]).on('change', function(file) {
	// server.changed(file.path);

});
