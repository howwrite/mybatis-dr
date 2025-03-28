package com.github.howwrite.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 动态SQL映射器接口，定义与数据库交互的方法
 *
 * @author mybatis-dr
 */
@Mapper
public interface DynamicSqlMapper {

    /**
     * 插入记录
     *
     * @param params 参数Map，包含表名和实体对象
     * @return 影响的行数
     */
    int insert(@Param("params") Map<String, Object> params);

    /**
     * 批量插入记录
     *
     * @param params 参数Map，包含表名和实体对象列表
     * @return 影响的行数
     */
    int batchInsert(@Param("params") Map<String, Object> params);

    /**
     * 更新记录
     *
     * @param params 参数Map，包含表名、实体对象和主键
     * @return 影响的行数
     */
    int update(@Param("params") Map<String, Object> params);

    /**
     * 根据条件删除记录
     *
     * @param params 参数Map，包含表名和条件对象
     * @return 影响的行数
     */
    int delete(@Param("params") Map<String, Object> params);

    /**
     * 更新记录
     *
     * @param params 参数Map，包含表名、实体对象和主键
     * @return 影响的行数
     */
    int logicDelete(@Param("params") Map<String, Object> params);

    /**
     * 根据条件查询记录
     *
     * @param params 参数Map，包含表名和条件对象
     * @return 实体对象列表
     */
    @MapKey("id")
    List<Map<String,Object>> findByCondition(@Param("params") Map<String, Object> params);


    /**
     * 根据条件查询记录数
     *
     * @param params 参数Map，包含表名和条件对象
     * @return 记录数
     */
    long count(@Param("params") Map<String, Object> params);
} 