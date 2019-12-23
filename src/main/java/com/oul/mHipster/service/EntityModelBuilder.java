package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
import com.oul.mHipster.model.SourceDomainLayer;

import java.util.Map;

public interface EntityModelBuilder {

    Entity mapSourceToEntity(Class<?> clazz);

    void buildLayers(SourceDomainLayer sourceDomainLayer);

    Map<String, LayerClass> buildLayerClass(Entity entity, SourceDomainLayer sourceDomainLayer);
}
