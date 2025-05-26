package com.github.howwrite.converter;

import java.lang.reflect.Type;

public interface DrConverter {
    /**
     * 序列化
     * java对象转数据库对象
     */
    Object serialize(Object value);

    /**
     * 反序列化
     * 数据库值转Java对象
     */
    Object deserialize(Type fieldType, Object value);
}
