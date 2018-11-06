package cn.mirrorming.springMVCdemo.framework.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author mirror
 * @version 创建时间：2018年11月1日 下午4:18:51
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface MirrorController {
	String value() default "";
}
