package com.github.howwrite.annotation;

import java.lang.annotation.*;

/**
 * 字段注解，用于指定实体字段映射的MySQL列名
 *
 * @author mybatis-dr
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DrField {

    /**
     * 数据库列名
     *
     * @return 列名
     */
    String value();
} 