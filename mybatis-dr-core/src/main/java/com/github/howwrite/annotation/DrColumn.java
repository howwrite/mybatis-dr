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
public @interface DrColumn {

    /**
     * 数据库列名
     *
     * @return 列名
     */
    String value();

    /**
     * 是否是查询字段
     * true表示对应单独的一列；false表示在feature json中，此时作用是改名
     */
    boolean query() default true;
} 