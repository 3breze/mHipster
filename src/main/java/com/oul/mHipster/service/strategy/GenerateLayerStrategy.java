package com.oul.mHipster.service.strategy;

import com.oul.mHipster.layerconfig.LayersConfig;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.util.Util;

public interface GenerateLayerStrategy {

    LayersConfig layersConfig = Util.getValue();
    EntityManagerService entityManagerService = EntityManagerFactory.getInstance();

    TypeSpecWrapper generate(Entity entity);

}
