package com.github.howwrite.util;

import com.alibaba.fastjson2.JSON;
import com.github.howwrite.annotation.DrField;
import com.github.howwrite.annotation.DrFieldIgnore;
import com.github.howwrite.annotation.DrTable;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            tableInfo.setFeatureColumnName(drTable.featureFiledName());
            tableInfo.setLogicDelete(drTable.logicDelete());
            tableInfo.setCreatedTimeColumnName(drTable.createdTimeColumnName());
            tableInfo.setUpdatedTimeColumnName(drTable.updatedTimeColumnName());

            // 解析字段
            Map<String, Field> fieldMap = new HashMap<>();
            List<Field> jsonFields = new ArrayList<>();

            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                // 跳过静态字段和被@FieldIgnore标记的字段
                if (Modifier.isStatic(field.getModifiers())
                        || field.isAnnotationPresent(DrFieldIgnore.class)) {
                    continue;
                }

                // 访问权限
                field.setAccessible(true);

                DrField drFieldAnnotation = field.getAnnotation(DrField.class);
                if (drFieldAnnotation != null) {
                    // 有@Field注解的字段
                    String columnName = drFieldAnnotation.value();
                    fieldMap.put(columnName, field);
                } else {
                    // 没有@Field注解的字段，添加到JSON字段列表
                    jsonFields.add(field);
                }
            }

            tableInfo.setFieldMap(fieldMap);
            tableInfo.setJsonFields(jsonFields);
            TABLE_INFO_CACHE.put(entityClass, tableInfo);
            return tableInfo;
        }
    }

    /**
     * 解析实体对象为Map，用于SQL操作
     *
     * @param entity    实体对象
     * @param tableInfo 表信息
     * @param create 是否是创建
     * @return 字段值Map
     */
    public static Map<String, Object> parseEntity(Object entity, TableInfo<?> tableInfo, boolean create) {
        if (entity == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> featureMap = new HashMap<>();

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

        // 处理JSON字段
        for (Field jsonField : tableInfo.getJsonFields()) {
            try {
                Object value = jsonField.get(entity);
                if (value != null) {
                    featureMap.put(jsonField.getName(), value);
                }
            } catch (Exception e) {
                LOGGER.error("Error getting JSON field value: " + jsonField.getName(), e);
            }
        }

        // 添加feature字段
        if (!featureMap.isEmpty()) {
            result.put(tableInfo.getFeatureColumnName(), JSON.toJSONString(featureMap));
        }

        result.put(tableInfo.getUpdatedTimeColumnName(), LocalDateTime.now());
        if (create) {
            result.put(tableInfo.getCreatedTimeColumnName(), LocalDateTime.now());
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
            if (featureJson != null) {
                Map<String, Object> jsonFields = JSON.parseObject(featureJson);
                for (Field jsonField : tableInfo.getJsonFields()) {
                    Object value = jsonFields.get(jsonField.getName());

                    if (value != null) {
                        assignField(jsonField, entity, value);
                    }
                }
            }

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity instance", e);
        }
    }

    public static void assignField(Field field, Object target, Object value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        if (fieldType.isInstance(value)) {
            field.set(target, value);
            return;
        }

        Object convertedValue = convertPrimitive(fieldType, value);
        if (convertedValue != null) {
            field.set(target, convertedValue);
        }
    }

    private static Object convertPrimitive(Class<?> fieldType, Object value) {
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
}