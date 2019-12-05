package com.oul.mHipster.service;

public interface LayerGeneratorService<T> {

    void create(Class<T> clazz);

    Class<T> findClassForName(String name);
}
