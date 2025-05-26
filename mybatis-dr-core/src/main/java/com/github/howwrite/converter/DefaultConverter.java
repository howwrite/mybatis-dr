package com.github.howwrite.converter;

import com.alibaba.fastjson2.JSON;
import com.github.howwrite.treasure.core.utils.NumberUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class DefaultConverter implements DrConverter {

    @Override
    public Object serialize(Object value) {
        return value;
    }

    @Override
    public Object deserialize(Type fieldType, Object value) {
        if (value == null) {
            return null;
        }
        if (fieldType == String.class) {
            return value.toString();
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return NumberUtils.buildBigDecimal(value.toString()).intValue();
        } else if (fieldType == double.class || fieldType == Double.class) {
            return NumberUtils.buildBigDecimal(value.toString()).doubleValue();
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.valueOf(value.toString());
        } else if (fieldType == float.class || fieldType == Float.class) {
            return NumberUtils.buildBigDecimal(value.toString()).floatValue();
        } else if (fieldType == Short.class || fieldType == short.class) {
            return NumberUtils.buildBigDecimal(value.toString()).shortValue();
        } else if (fieldType == char.class || fieldType == Character.class) {
            return value.toString().charAt(0);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return NumberUtils.buildBigDecimal(value.toString()).longValue();
        } else if (fieldType == BigDecimal.class) {
            return NumberUtils.buildBigDecimal(value.toString());
        } else {
            String jsonStr = JSON.toJSONString(value);
            return JSON.parseObject(jsonStr, fieldType);
        }
    }
}
