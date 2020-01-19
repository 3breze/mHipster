package com.oul.mHipster.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public class ReflectionUtil {

    public static Class<?> resolveTypeArgument(Field field) {
        if (field.getType().isAssignableFrom(Collection.class)) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return field.getType();
    }
}
