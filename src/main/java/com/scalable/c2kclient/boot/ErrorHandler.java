package com.scalable.c2kclient.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以用来处理异常
 *
 * @author Robert Lee
 * @since Aug 21, 2015
 *
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ErrorHandler {
	Class<? extends Throwable> exception() default Throwable.class;

	String topic() default "";
}