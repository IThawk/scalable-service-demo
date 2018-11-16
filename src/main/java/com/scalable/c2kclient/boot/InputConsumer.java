package com.scalable.c2kclient.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表这个方法的参数来自于一个消息队列，
 * 包括：连接消息队列的属性、队列名称、流的数量、固定线程池的线程数、可变线程池的最大、最小线程数等属性
 *
 * @author Robert Lee
 * @since Aug 21, 2015
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InputConsumer {
	String propertiesFile() default "";

	String topic() default "";

	int streamNum() default 1;

	int fixedThreadNum() default 0;

	int minThreadNum() default 0;

	int maxThreadNum() default 0;
}