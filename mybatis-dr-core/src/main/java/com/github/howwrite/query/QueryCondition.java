package com.github.howwrite.query;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 查询条件基础类，用于生成查询条件
 *
 * @author mybatis-dr
 */
public abstract class QueryCondition {

    /**
     * 等于
     */
    public static final String OPERATOR_EQ = "=";

    /**
     * 不等于
     */
    public static final String OPERATOR_NE = "!=";

    /**
     * 大于
     */
    public static final String OPERATOR_GT = ">";

    /**
     * 大于等于
     */
    public static final String OPERATOR_GE = ">=";

    /**
     * 小于
     */
    public static final String OPERATOR_LT = "<";

    /**
     * 小于等于
     */
    public static final String OPERATOR_LE = "<=";

    /**
     * 包含
     */
    public static final String OPERATOR_IN = "IN";

    /**
     * 不包含
     */
    public static final String OPERATOR_NOT_IN = "NOT IN";

    /**
     * 模糊匹配
     */
    public static final String OPERATOR_LIKE = "LIKE";

    /**
     * 为NULL
     */
    public static final String OPERATOR_IS_NULL = "IS NULL";

    /**
     * 不为NULL
     */
    public static final String OPERATOR_IS_NOT_NULL = "IS NOT NULL";

    /**
     * 条件列表
     */
    private final List<Condition> conditions = new ArrayList<>();

    /**
     * 排序字段列表
     */
    private final List<Order> orders = new ArrayList<>();

    private Integer limit;

    private Integer offset;

    /**
     * @return 当前实体类型
     */
    public abstract Class<?> currentEntityClass();

    public void setPageInfo(int pageNo, int pageSize) {
        offset = (pageNo - 1) * pageSize;
        limit = pageSize;
    }

    /**
     * 添加条件
     *
     * @param field    字段名
     * @param operator 操作符
     * @param value    值
     * @return 当前对象
     */
    public QueryCondition addCondition(String field, String operator, Object value) {
        conditions.add(new Condition(field, operator, value));
        return this;
    }

    /**
     * 添加等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition eq(String field, Object value) {
        return addCondition(field, OPERATOR_EQ, value);
    }

    /**
     * 添加不等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition ne(String field, Object value) {
        return addCondition(field, OPERATOR_NE, value);
    }

    /**
     * 添加大于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition gt(String field, Object value) {
        return addCondition(field, OPERATOR_GT, value);
    }

    /**
     * 添加大于等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition ge(String field, Object value) {
        return addCondition(field, OPERATOR_GE, value);
    }

    /**
     * 添加小于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition lt(String field, Object value) {
        return addCondition(field, OPERATOR_LT, value);
    }

    /**
     * 添加小于等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition le(String field, Object value) {
        return addCondition(field, OPERATOR_LE, value);
    }

    /**
     * 添加IN条件
     *
     * @param field  字段名
     * @param values 值集合
     * @return 当前对象
     */
    public QueryCondition in(String field, Collection<?> values) {
        return addCondition(field, OPERATOR_IN, values);
    }

    /**
     * 添加NOT IN条件
     *
     * @param field  字段名
     * @param values 值集合
     * @return 当前对象
     */
    public QueryCondition notIn(String field, Collection<?> values) {
        return addCondition(field, OPERATOR_NOT_IN, values);
    }

    /**
     * 添加LIKE条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition like(String field, String value) {
        return addCondition(field, OPERATOR_LIKE, "%" + value + "%");
    }

    /**
     * 添加左LIKE条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition likeLeft(String field, String value) {
        return addCondition(field, OPERATOR_LIKE, "%" + value);
    }

    /**
     * 添加右LIKE条件
     *
     * @param field 字段名
     * @param value 值
     * @return 当前对象
     */
    public QueryCondition likeRight(String field, String value) {
        return addCondition(field, OPERATOR_LIKE, value + "%");
    }

    /**
     * 添加IS NULL条件
     *
     * @param field 字段名
     * @return 当前对象
     */
    public QueryCondition isNull(String field) {
        return addCondition(field, OPERATOR_IS_NULL, null);
    }

    /**
     * 添加IS NOT NULL条件
     *
     * @param field 字段名
     * @return 当前对象
     */
    public QueryCondition isNotNull(String field) {
        return addCondition(field, OPERATOR_IS_NOT_NULL, null);
    }

    public QueryCondition addOrder(String field, String orderMode) {
        orders.add(new Order(field, orderMode));
        return this;
    }

    public QueryCondition desc(String field) {
        return addOrder(field, "desc");
    }


    public QueryCondition asc(String field) {
        return addOrder(field, "asc");
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * 内部排序类
     */
    public static class Order {
        /**
         * 字段名
         */
        private final String field;
        /**
         * 排序字段
         */
        private final String orderMode;

        public Order(String field, String orderMode) {
            this.field = field;
            this.orderMode = orderMode;
        }

        public String getField() {
            return field;
        }

        public String getOrderMode() {
            return orderMode;
        }
    }

    /**
     * 内部条件类
     */
    public static class Condition {
        /**
         * 字段名
         */
        private String field;

        /**
         * 操作符
         */
        private String operator;

        /**
         * 值
         */
        private Object value;

        /**
         * 构造函数
         *
         * @param field    字段名
         * @param operator 操作符
         * @param value    值
         */
        public Condition(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}