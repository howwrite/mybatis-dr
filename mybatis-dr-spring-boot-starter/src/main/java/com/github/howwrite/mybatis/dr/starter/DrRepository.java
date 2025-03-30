package com.github.howwrite.mybatis.dr.starter;

import com.github.howwrite.mapper.DynamicSqlMapper;
import com.github.howwrite.query.QueryCondition;
import com.github.howwrite.treasure.spring.utils.SpringUtils;
import com.github.howwrite.util.EntityHelper;
import com.github.howwrite.util.TableInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DrRepository {

    /**
     * 表信息
     */
    private static final Map<Class<?>, TableInfo<?>> tableInfoMap = new ConcurrentHashMap<>();

    /**
     * 动态Mapper
     */
    private static DynamicSqlMapper getDynamicSqlMapper() {
        return SpringUtils.getBean(DynamicSqlMapper.class);
    }

    private static TableInfo<?> getTableInfo(Class<?> entityClass) {
        TableInfo<?> tableInfo = tableInfoMap.get(entityClass);
        if (tableInfo == null) {
            tableInfo = EntityHelper.getTableInfo(entityClass);
            tableInfoMap.put(entityClass, tableInfo);
        }
        return tableInfo;
    }


    public static int insert(Object entity) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, true));
        return getDynamicSqlMapper().insert(params);
    }

    public static int insertOrUpdate(Object entity) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, true));
        return getDynamicSqlMapper().insertOrUpdate(params);
    }


    public static int batchInsert(List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        TableInfo<?> tableInfo = getTableInfo(entities.getFirst().getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entities", EntityHelper.parseEntities(entities, tableInfo, true));
        return getDynamicSqlMapper().batchInsert(params);
    }


    public static int batchInsertOrUpdate(List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        TableInfo<?> tableInfo = getTableInfo(entities.getFirst().getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("entities", EntityHelper.parseEntities(entities, tableInfo, true));
        params.put("tableName", tableInfo.getTableName());
        return getDynamicSqlMapper().batchInsertOrUpdate(params);
    }


    public static int update(Object entity, QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, false));
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().update(params);
    }


    public static int delete(QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(condition.currentEntityClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        if (Boolean.TRUE.equals(tableInfo.getLogicDelete())) {
            return getDynamicSqlMapper().logicDelete(params);
        }
        return getDynamicSqlMapper().delete(params);
    }


    public static <T> List<T> findByCondition(QueryCondition<T> condition) {
        TableInfo<T> tableInfo = (TableInfo<T>) getTableInfo(condition.currentEntityClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().findByCondition(params)
                .stream().map(it -> EntityHelper.convertToEntity(it, tableInfo)).collect(Collectors.toList());
    }


    public static long count(QueryCondition condition) {
        TableInfo<?> tableInfo = getTableInfo(condition.currentEntityClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().count(params);
    }
}
