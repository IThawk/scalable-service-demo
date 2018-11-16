package com.scalable.c2kclient.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表这个方法的返回值会被自动发送给另一个消息队列
 * 包括：连接消息队列的信息和默认的队列名称等属性
 *
 * @author Robert Lee
 * @since Aug 21, 2015
 *
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OutputProducer {
	String propertiesFile() default "";

	String defaultTopic() default "";
}