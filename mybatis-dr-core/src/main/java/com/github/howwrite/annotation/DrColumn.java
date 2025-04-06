package com.github.howwrite.annotation;

import com.github.howwrite.mapper.DynamicSqlMapper;

import java.lang.annotation.*;
import java.util.Map;

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

    /**
     * 写入冲突时是否更新，使用以下两个api时可用
     * {@link DynamicSqlMapper#insertOrUpdate(Map)}
     * {@link DynamicSqlMapper#batchInsertOrUpdate(Map)}
     * <p>
     * 默认都是冲突时更新，仅当{@link #query()} == true时可用，不是true时是在json中
     * <p>
     * 创建时间会特殊处理，默认索引冲突时不会更新，需要更新时除了将此配置置为true，还需要更改这个api {@link DrTable#whenDuplicateUpdateCreatedTime()}
     */
    boolean whenDuplicateUpdate() default true;
} 