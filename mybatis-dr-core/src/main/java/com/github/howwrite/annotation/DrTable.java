package com.github.howwrite.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表注解，用于指定实体对应的MySQL表名
 * 
 * @author mybatis-dr
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DrTable {
    
    /**
     * 表名
     * 
     * @return 数据库表名
     */
    String value();

    /**
     * @return 数据库中 feature字段名称
     */
    String featureFiledName() default "feature";

    /**
     * @return 是否逻辑删除
     */
    boolean logicDelete() default false;
} 