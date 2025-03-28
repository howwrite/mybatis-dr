package com.github.howwrite.mybatis.dr.starter.autoconfigure;

import com.github.howwrite.mapper.DynamicSqlMapper;
import com.github.howwrite.query.QueryCondition;
import com.github.howwrite.util.EntityHelper;
import com.github.howwrite.util.TableInfo;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DrRepository {

    /**
     * 表信息
     */
    private final Map<Class<?>, TableInfo<?>> tableInfoMap = new ConcurrentHashMap<>();
    /**
     * 动态Mapper
     */
    @Resource
    private DynamicSqlMapper dynamicSqlMapper;

    private TableInfo<?> getTableInfo(Class<?> entityClass) {
        TableInfo<?> tableInfo = tableInfoMap.get(entityClass);
        if (tableInfo == null) {
            tableInfo = EntityHelper.getTableInfo(entityClass);
            tableInfoMap.put(entityClass, tableInfo);
        }
        return tableInfo;
    }


    public int insert(Object entity) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, true));
        return dynamicSqlMapper.insert(params);
    }


    public int batchInsert(List<Object> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        TableInfo<?> tableInfo = getTableInfo(entities.getFirst().getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entities", EntityHelper.parseEntities(entities, tableInfo, true));
        return dynamicSqlMapper.batchInsert(params);
    }


    public int update(Object entity, QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, false));
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return dynamicSqlMapper.update(params);
    }


    public int delete(Class<?> clazz, QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(clazz);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        if (Boolean.TRUE.equals(tableInfo.getLogicDelete())) {
            return dynamicSqlMapper.logicDelete(params);
        }
        return dynamicSqlMapper.delete(params);
    }


    public <T> List<T> findByCondition(Class<T> clazz, QueryCondition condition) {
        TableInfo<T> tableInfo = (TableInfo<T>) getTableInfo(clazz);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return dynamicSqlMapper.findByCondition(params)
                .stream().map(it -> EntityHelper.convertToEntity(it, tableInfo)).collect(Collectors.toList());
    }


    public long count(Class<?> clazz, QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(clazz);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return dynamicSqlMapper.count(params);
    }
}
