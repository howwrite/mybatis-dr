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
     * feature字段数据库中的列名
     */
    private String featureColumnName;

    /**
     * 创建时间字段数据库中的列名
     * `created_time` datetime        not null default now() comment '创建时间'
     */
    private String createdTimeColumnName;
    /**
     * 更新时间字段数据库中的列名
     *`updated_time` datetime        not null default now() on update current_timestamp comment '记录修改时间'
     */
    private String updatedTimeColumnName;

    /**
     * 实体类
     */
    private Class<T> entityClass;

    /**
     * 逻辑删除
     */
    private Boolean logicDelete;

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

    public String getFeatureColumnName() {
        return featureColumnName;
    }

    public void setFeatureColumnName(String featureColumnName) {
        this.featureColumnName = featureColumnName;
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

    public String getCreatedTimeColumnName() {
        return createdTimeColumnName;
    }

    public void setCreatedTimeColumnName(String createdTimeColumnName) {
        this.createdTimeColumnName = createdTimeColumnName;
    }

    public String getUpdatedTimeColumnName() {
        return updatedTimeColumnName;
    }

    public void setUpdatedTimeColumnName(String updatedTimeColumnName) {
        this.updatedTimeColumnName = updatedTimeColumnName;
    }

    public Boolean getLogicDelete() {
        return logicDelete;
    }

    public void setLogicDelete(Boolean logicDelete) {
        this.logicDelete = logicDelete;
    }
}