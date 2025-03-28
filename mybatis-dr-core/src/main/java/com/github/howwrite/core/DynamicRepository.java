package com.github.howwrite.core;

import com.github.howwrite.query.QueryCondition;

import java.util.List;

/**
 * 动态仓库接口，定义基本的CRUD操作
 * 
 * @param <T> 实体类型
 * @author mybatis-dr
 */
public interface DynamicRepository<T> {
    
    /**
     * 插入一条记录
     * 
     * @param entity 实体对象
     * @return 影响的行数
     */
    int insert(T entity);
    
    /**
     * 批量插入记录
     * 
     * @param entities 实体对象列表
     * @return 影响的行数
     */
    int batchInsert(List<T> entities);
    
    /**
     * 根据主键更新记录
     * 
     * @param entity 实体对象
     * @param condition 条件对象
     * @return 影响的行数
     */
    int update(T entity, QueryCondition condition);
    
    /**
     * 根据条件删除记录
     * 
     * @param condition 条件对象
     * @return 影响的行数
     */
    int delete(QueryCondition condition);
    
    /**
     * 根据条件查询记录
     * 
     * @param condition 条件对象
     * @return 实体对象列表
     */
    List<T> findByCondition(QueryCondition condition);
    
    /**
     * 根据条件查询记录数
     * 
     * @param condition 条件对象
     * @return 记录数
     */
    long count(QueryCondition condition);
} 