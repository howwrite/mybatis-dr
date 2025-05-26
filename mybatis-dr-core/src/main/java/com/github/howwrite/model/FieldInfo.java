package com.github.howwrite.model;

import com.github.howwrite.converter.DrConverter;

import java.lang.reflect.Field;

public record FieldInfo(Field field, Class<? extends DrConverter> drConverterClass) {
}
