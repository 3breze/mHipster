package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.SourceDomainLayer;

import java.util.Map;

public interface EntityModelBuilder {

    Entity mapSourceToEntity(Class<?> clazz);

    void buildLayers(SourceDomainLayer sourceDomainLayer);

    Map<String, ClassNamingInfo> buildLayerClass(Entity entity, SourceDomainLayer sourceDomainLayer);
}
