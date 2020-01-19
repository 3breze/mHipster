package com.oul.mHipster.service;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.impl.EntityManagerServiceImpl;

public final class EntityManagerFactory {

    private static EntityManagerService instance;
    private static LayerModelWrapper layerModelWrapper;

    private EntityManagerFactory() {
    }

    public static void createEntityManager(LayerModelWrapper layerModelWrapper) {
        EntityManagerFactory.layerModelWrapper = layerModelWrapper;
    }

    public static EntityManagerService getInstance() {
        // instance creation logic, same as singleton creation logic
        if (instance == null) {
            instance = new EntityManagerServiceImpl();
            instance.setLayerModel(layerModelWrapper);
        }
        return instance;
    }
}
