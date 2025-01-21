package com.yjh.sql.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author osmondy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface QueryCondition {

    /**
     * 数据库中字段名,默认为空字符串,则Query类中的字段要与数据库中字段一致
     * @return
     */
    String column() default "";

    /**
     * equal, like, gt, lt...
     * @return
     */
    MatchType func() default MatchType.equal;

    /**
     * object是否可以为null
     * @return
     */
    boolean nullable() default false;

    /**
     * 字符串是否可为空
     * @return
     */
    boolean emptyable() default false;

}
