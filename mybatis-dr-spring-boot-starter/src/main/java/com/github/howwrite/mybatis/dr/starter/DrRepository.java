package com.github.howwrite.mybatis.dr.starter;

import com.github.howwrite.constant.MybatisDrContent;
import com.github.howwrite.mapper.DynamicSqlMapper;
import com.github.howwrite.model.FieldInfo;
import com.github.howwrite.query.QueryCondition;
import com.github.howwrite.treasure.spring.utils.SpringUtils;
import com.github.howwrite.util.EntityHelper;
import com.github.howwrite.util.TableInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        int insertResult = getDynamicSqlMapper().insert(params);
        writeAutoGenId(tableInfo, entity, params);
        return insertResult;
    }

    public static int insertOrUpdate(Object entity) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, true));
        params.put("whenDuplicateUpdateFields", tableInfo.getWhenDuplicateUpdateFields());
        int insertResult = getDynamicSqlMapper().insertOrUpdate(params);
        writeAutoGenId(tableInfo, entity, params);
        return insertResult;
    }


    public static int batchInsert(List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        TableInfo<?> tableInfo = getTableInfo(entities.getFirst().getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        List<Map<String, Object>> entitiesParam = EntityHelper.parseEntities(entities, tableInfo, true);
        int insertResult = getDynamicSqlMapper().batchInsert(params, entitiesParam);
        for (int i = 0; i < entities.size(); i++) {
            Object entity = entities.get(i);
            Map<String, Object> entityParam = entitiesParam.get(i);
            writeAutoGenId(tableInfo, entity, entityParam);
        }
        return insertResult;
    }


    public static int batchInsertOrUpdate(List<?> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        TableInfo<?> tableInfo = getTableInfo(entities.getFirst().getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("whenDuplicateUpdateFields", tableInfo.getWhenDuplicateUpdateFields());
        List<Map<String, Object>> entitiesParam = EntityHelper.parseEntities(entities, tableInfo, true);
        return getDynamicSqlMapper().batchInsertOrUpdate(params, entitiesParam);
    }


    public static int update(Object entity, QueryCondition<?> condition) {
        TableInfo<?> tableInfo = getTableInfo(entity.getClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("entity", EntityHelper.parseEntity(entity, tableInfo, false));
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().update(params);
    }


    public static int delete(QueryCondition<?> condition) {
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


    public static <T> Optional<T> findOne(QueryCondition<T> condition) {
        TableInfo<T> tableInfo = (TableInfo<T>) getTableInfo(condition.currentEntityClass());
        condition.setLimit(1);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().findByCondition(params).stream().map(it -> EntityHelper.convertToEntity(it, tableInfo)).findFirst();
    }

    public static long count(QueryCondition<?> condition) {
        TableInfo<?> tableInfo = getTableInfo(condition.currentEntityClass());
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableInfo.getTableName());
        params.put("condition", condition);
        params.put("logicDelete", tableInfo.getLogicDelete());
        return getDynamicSqlMapper().count(params);
    }

    private static void writeAutoGenId(TableInfo<?> tableInfo, Object entity, Map<String, Object> params) {
        FieldInfo idField = tableInfo.getIdField();
        if (idField == null) {
            return;
        }
        Object genId = params.get(MybatisDrContent.AUTO_GEN_ID_FIELD_NAME);
        if (genId == null) {
            return;
        }
        try {
            EntityHelper.assignField(idField, entity, genId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
