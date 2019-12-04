package com.oul.mHipster;

public interface LayerGenerator<T> {

    void createTable(Class<T> clazz);

    Class<T> findClassForName(String name);
}
