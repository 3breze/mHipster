package com.oul.mHipster.service;

public class LayerGeneratorServiceImpl implements LayerGeneratorService {

    @Override
    public void create(Class clazz) {

    }

    @Override
    public Class findClassForName(String name) {
        try {
            Class<?> clazz = Class.forName("com.bla.TestActivity");
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
