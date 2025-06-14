package com.github.howwrite.model;

import com.github.howwrite.converter.DrConverter;

import java.lang.reflect.Field;

public class FieldInfo {
    private Field field;
    private Class<? extends DrConverter> drConverterClass;

    public FieldInfo(Field field, Class<? extends DrConverter> drConverterClass) {
        this.field = field;
        this.drConverterClass = drConverterClass;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Class<? extends DrConverter> getDrConverterClass() {
        return drConverterClass;
    }

    public void setDrConverterClass(Class<? extends DrConverter> drConverterClass) {
        this.drConverterClass = drConverterClass;
    }
}
