package com.github.howwrite.util;

import com.alibaba.fastjson2.JSON;
import com.github.howwrite.annotation.DrColumn;
import com.github.howwrite.annotation.DrColumnIgnore;
import com.github.howwrite.annotation.DrTable;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体工具类，用于处理实体与表的映射关系
 *
 * @author mybatis-dr
 */
public class EntityHelper {

    private static final Log LOGGER = LogFactory.getLog(EntityHelper.class);

    /**
     * 表信息缓存，key为实体类，value为表信息
     */
    private static final Map<Class<?>, TableInfo<?>> TABLE_INFO_CACHE = new ConcurrentHashMap<>();


    /**
     * 获取表信息
     *
     * @param entityClass 实体类
     * @return 表信息
     */
    public static <T> TableInfo<T> getTableInfo(Class<T> entityClass) {
        TableInfo<T> existValue = (TableInfo<T>) TABLE_INFO_CACHE.get(entityClass);
        if (existValue != null) {
            return existValue;
        }
        synchronized (entityClass) {
            TableInfo<T> existValue1 = (TableInfo<T>) TABLE_INFO_CACHE.get(entityClass);
            if (existValue1 != null) {
                return existValue1;
            }
            TableInfo<T> tableInfo = new TableInfo<>();
            tableInfo.setEntityClass(entityClass);

            // 解析表名
            DrTable drTable = entityClass.getAnnotation(DrTable.class);
            tableInfo.setTableName(drTable.value());
            tableInfo.setFeatureColumnName(drTable.featureColumnName());
            tableInfo.setLogicDelete(drTable.logicDelete());
            tableInfo.setCreatedTimeColumnName(drTable.createdTimeColumnName());
            tableInfo.setUpdatedTimeColumnName(drTable.updatedTimeColumnName());
            tableInfo.setIdColumnName(drTable.idColumnName());

            // 解析字段
            Map<String, Field> fieldMap = new HashMap<>();
            Map<String, Field> jsonFields = new HashMap<>();
            Field idField = null;
            Set<String> whenDuplicateUpdateFields = new HashSet<>();

            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                // 跳过静态字段和被@FieldIgnore标记的字段
                if (Modifier.isStatic(field.getModifiers())
                        || field.isAnnotationPresent(DrColumnIgnore.class)) {
                    continue;
                }

                // 访问权限
                field.setAccessible(true);

                DrColumn drColumnAnnotation = field.getAnnotation(DrColumn.class);
                if (drColumnAnnotation != null && drColumnAnnotation.query()) {
                    // 有@Field注解且是query的字段
                    String columnName = drColumnAnnotation.value();
                    fieldMap.put(columnName, field);

                    if (drTable.idColumnName().equals(columnName)) {
                        idField = field;
                        whenDuplicateUpdateFields.add(columnName);
                    }

                    // 冲突需要更新的字段处理 createTime字段看Table配置，非createdTime看字段配置
                    if ((drTable.createdTimeColumnName().equals(columnName) && drTable.whenDuplicateUpdateCreatedTime())
                            || (!drTable.createdTimeColumnName().equals(columnName) && drColumnAnnotation.whenDuplicateUpdate())
                    ) {
                        whenDuplicateUpdateFields.add(columnName);
                    }
                } else {
                    // 否则是json中的字段
                    String columnName = Optional.ofNullable(drColumnAnnotation).map(DrColumn::value).orElse(null);
                    if (columnName == null || columnName.isBlank()) {
                        columnName = field.getName();
                    }
                    jsonFields.put(columnName, field);
                }
            }

            tableInfo.setFieldMap(fieldMap);
            tableInfo.setJsonFieldMap(jsonFields);
            tableInfo.setWhenDuplicateUpdateFields(whenDuplicateUpdateFields);
            tableInfo.setIdField(idField);
            TABLE_INFO_CACHE.put(entityClass, tableInfo);
            return tableInfo;
        }
    }

    /**
     * 解析实体对象为Map，用于SQL操作
     *
     * @param entity    实体对象
     * @param tableInfo 表信息
     * @param create    是否是创建
     * @return 字段值Map
     */
    public static Map<String, Object> parseEntity(Object entity, TableInfo<?> tableInfo, boolean create) {
        if (entity == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> featureMap = new HashMap<>();

        // 填充时间，支持自定义填充时间，如果没有自定义填充时间，那么会使用当前时间用于创建、更新时间
        result.put(tableInfo.getUpdatedTimeColumnName(), LocalDateTime.now());
        if (create) {
            result.put(tableInfo.getCreatedTimeColumnName(), LocalDateTime.now());
        }

        // 处理普通字段
        for (Map.Entry<String, Field> entry : tableInfo.getFieldMap().entrySet()) {
            String columnName = entry.getKey();
            Field field = entry.getValue();

            try {
                Object value = field.get(entity);
                if (value != null) {
                    result.put(columnName, value);
                }
            } catch (Exception e) {
                LOGGER.error("Error getting field value: " + field.getName(), e);
            }
        }

        // 处理json字段
        for (Map.Entry<String, Field> entry : tableInfo.getJsonFieldMap().entrySet()) {
            String columnName = entry.getKey();
            Field field = entry.getValue();

            try {
                Object value = field.get(entity);
                if (value != null) {
                    featureMap.put(columnName, value);
                }
            } catch (Exception e) {
                LOGGER.error("Error getting json field value: " + field.getName(), e);
            }
        }

        // 添加feature字段
        if (!featureMap.isEmpty()) {
            result.put(tableInfo.getFeatureColumnName(), JSON.toJSONString(featureMap));
        }
        return result;
    }

    /**
     * 批量解析实体对象为Map列表
     *
     * @param entities  实体对象列表
     * @param tableInfo 表信息
     * @return 字段值Map列表
     */
    public static List<Map<String, Object>> parseEntities(List<?> entities, TableInfo<?> tableInfo, boolean create) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (entities != null && !entities.isEmpty()) {
            for (Object entity : entities) {
                result.add(parseEntity(entity, tableInfo, create));
            }
        }
        return result;
    }

    public static <T> T convertToEntity(Map<String, Object> map, TableInfo<T> tableInfo) {
        if (map == null) {
            return null;
        }

        try {
            T entity = tableInfo.getEntityClass().getDeclaredConstructor().newInstance();

            // 处理带注解的字段
            for (Map.Entry<String, Field> entry : tableInfo.getFieldMap().entrySet()) {
                String columnName = entry.getKey();
                Field field = entry.getValue();
                Object value = map.get(columnName);

                if (value != null) {
                    assignField(field, entity, value);
                }
            }

            // 处理JSON字段
            String featureJson = (String) map.get(tableInfo.getFeatureColumnName());
            if (featureJson != null && !featureJson.isBlank()) {
                Map<String, Object> jsonFields = JSON.parseObject(featureJson);
                for (Map.Entry<String, Field> entry : tableInfo.getJsonFieldMap().entrySet()) {
                    String columnName = entry.getKey();
                    Field field = entry.getValue();
                    Object value = jsonFields.get(columnName);

                    if (value != null) {
                        assignField(field, entity, value);
                    }
                }
            }

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity instance", e);
        }
    }

    public static void assignField(Field field, Object target, Object value) throws IllegalAccessException {
        Type fieldType = field.getGenericType();

        if (isTypeCompatible(field, value)) {
            field.set(target, value);
            return;
        }

        Object convertedValue = convertPrimitive(fieldType, value);
        if (convertedValue != null) {
            field.set(target, convertedValue);
        }
    }

    private static Object convertPrimitive(Type fieldType, Object value) {
        if (value == null) {
            return null;
        }
        if (fieldType == String.class) {
            return value.toString();
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.valueOf(value.toString());
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.valueOf(value.toString());
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.valueOf(value.toString());
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.valueOf(value.toString());
        } else if (fieldType == Short.class || fieldType == short.class) {
            return Short.valueOf(value.toString());
        } else if (fieldType == char.class || fieldType == Character.class) {
            return value.toString().charAt(0);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.valueOf(value.toString());
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(value.toString());
        } else {
            String jsonStr = JSON.toJSONString(value);
            return JSON.parseObject(jsonStr, fieldType);
        }
    }

    // 检查 Field 对象的类型和对象的类型是否兼容
    public static boolean isTypeCompatible(Field field, Object obj) {
        if (obj == null) {
            return true; // 空对象被认为与任何类型兼容
        }

        // 获取 Field 的类型
        Type fieldType = field.getGenericType();
        Class<?> fieldRawType;
        if (fieldType instanceof ParameterizedType) {
            fieldRawType = (Class<?>) ((ParameterizedType) fieldType).getRawType();
        } else {
            fieldRawType = field.getType();
        }

        // 检查对象的类型是否是 Field 原始类型的子类
        if (!fieldRawType.isAssignableFrom(obj.getClass())) {
            return false;
        }

        // 如果 Field 类型是泛型类型
        if (fieldType instanceof ParameterizedType parameterizedFieldType) {
            Type[] fieldTypeArguments = parameterizedFieldType.getActualTypeArguments();

            // 如果对象也是泛型类型
            if (obj.getClass().getGenericSuperclass() instanceof ParameterizedType parameterizedObjType) {
                Type[] objTypeArguments = parameterizedObjType.getActualTypeArguments();

                // 检查泛型类型参数是否兼容
                if (fieldTypeArguments.length != objTypeArguments.length) {
                    return false;
                }
                for (int i = 0; i < fieldTypeArguments.length; i++) {
                    if (!fieldTypeArguments[i].equals(objTypeArguments[i])) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}