package com.oul.mHipster.service.model;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;

public interface EntityManagerService {

    void setLayerModel(LayerModelWrapper layerModelWrapper);

    TypeWrapper getProperty(String entityName, String layerName);

    TypeWrapper getProperty(String entityName, String typeArgument, String instanceName);

}
