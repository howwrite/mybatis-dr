package com.github.howwrite.annotation;

import java.lang.annotation.*;

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
     * @return 是否逻辑删除
     * <p>
     * `deleted`      bigint unsigned not null default 0 comment '逻辑删除',
     * `deleted_time` datetime                 default null comment '删除时间'
     */
    boolean logicDelete() default false;

    /**
     * 数据库中id字段名称
     */
    String idColumnName() default "id";

    /**
     * @return 数据库中 feature字段名称
     * `feature`      longtext        comment '特性'
     */
    String featureColumnName() default "feature";

    /**
     * 创建时间字段数据库中的列名
     * `created_time` datetime        not null default now() comment '创建时间'
     */
    String createdTimeColumnName() default "created_time";

    /**
     * 更新时间字段数据库中的列名
     * `updated_time` datetime        not null default now() on update current_timestamp comment '记录修改时间'
     */
    String updatedTimeColumnName() default "updated_time";

    /**
     * 唯一索引冲突时 创建时间是否更新
     */
    boolean whenDuplicateUpdateCreatedTime() default false;

} 