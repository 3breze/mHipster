package com.oul.mHipster.service.strategy;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.util.Util;

public interface GenerateLayerStrategy {

    LayersConfig layersConfig = Util.getValue();
    EntityManagerService entityManagerService = EntityManagerFactory.getInstance();

    TypeSpecWrapper generate(Entity entity);

}
