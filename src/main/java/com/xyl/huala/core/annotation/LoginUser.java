package com.xyl.huala.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录用户权限判定
 * 
 * @author zxl0047
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
	/**
	 * 用户角色名称：<br>
	 * 如果为空，则不管角色类型，全部做登录处理<br>
	 * 如果值不为空，则判断角色类型是否匹配
	 * @return
	 */
	String[] value() default {};

	/**
	 * 是否需要重定向。<br>
	 * 正常情况下登录则重定向,当自动登录时，无论成功或不成功不处理，则此值为false
	 * 
	 * @return
	 */
	boolean redirect() default true;
}
