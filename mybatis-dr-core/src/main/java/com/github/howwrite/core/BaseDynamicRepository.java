package com.github.howwrite.core;

import com.github.howwrite.mapper.DynamicSqlMapper;
import com.github.howwrite.query.QueryCondition;
import com.github.howwrite.util.EntityHelper;
import com.github.howwrite.util.TableInfo;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态仓库基础实现类
 *
 * @param <T> 实体类型
 * @author mybatis-dr
 */
public class BaseDynamicRepository<T> implements DynamicRepository<T> {

    /**
     * MyBatis的SqlSession
     */
    protected SqlSession sqlSession;

    /**
     * 表信息
     */
    protected TableInfo<T> tableInfo;

    /**
     * 动态Mapper
     */
    protected DynamicSqlMapper dynamicSqlMapper;

    /**
     * 构造函数
     *
     * @param sqlSession MyBatis的SqlSession
     */
    @SuppressWarnings("unchecked")
    public BaseDynamicRepository(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<T> entityClass = (Class<T>) params[0];
        this.tableInfo = EntityHelper.getTableInfo(entityClass);
        this.dynamicSqlMapper = sqlSession.getMapper(DynamicSqlMapper.class);
    }

    @Override
    public int insert(T entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo));
        return dynamicSqlMapper.insert(params);
    }

    @Override
    public int batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entities", EntityHelper.parseEntities(entities, tableInfo));
        return dynamicSqlMapper.batchInsert(params);
    }

    @Override
    public int update(T entity, QueryCondition condition) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo));
        params.put("condition", condition);
        return dynamicSqlMapper.update(params);
    }


    @Override
    public int delete(QueryCondition condition) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        return dynamicSqlMapper.delete(params);
    }

    @Override
    public List<T> findByCondition(QueryCondition condition) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        return dynamicSqlMapper.findByCondition(params)
                .stream().map(it -> EntityHelper.convertToEntity(it, tableInfo)).collect(Collectors.toList());
    }

    @Override
    public long count(QueryCondition condition) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        return dynamicSqlMapper.count(params);
    }
} 