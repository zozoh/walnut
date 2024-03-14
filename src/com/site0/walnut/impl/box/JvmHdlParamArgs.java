package com.site0.walnut.impl.box;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 JvmHdl 的子类上，可以声明这个注解，以便定制解析参数的模式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface JvmHdlParamArgs {

    public String value() default "";

    public String regex() default "";

}
