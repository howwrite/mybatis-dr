package com.github.howwrite.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 表信息类，存储实体类与表的映射信息
 *
 * @author mybatis-dr
 */
public class TableInfo<T> {

    /**
     * 表名
     */
    private String tableName;

    /**
     * feature字段名称
     */
    private String featureFiledName;

    /**
     * 实体类
     */
    private Class<T> entityClass;

    /**
     * 字段映射，key为数据库列名，value为Java的成员反射
     */
    private Map<String, Field> fieldMap;

    /**
     * JSON字段列表，这些字段将被合并到feature字段中
     */
    private List<Field> jsonFields;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFeatureFiledName() {
        return featureFiledName;
    }

    public void setFeatureFiledName(String featureFiledName) {
        this.featureFiledName = featureFiledName;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public List<Field> getJsonFields() {
        return jsonFields;
    }

    public void setJsonFields(List<Field> jsonFields) {
        this.jsonFields = jsonFields;
    }
}