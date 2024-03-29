package com.github.ds67.jminicache.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        type = ((ParameterizedType)superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}