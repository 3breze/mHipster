package com.oul.mHipster.service.model;

import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;

import java.util.Map;

public interface EntityManagerService {

    void setLayerModel(LayerModelWrapper layerModelWrapper);

    FieldTypeNameWrapper getProperty(String entityName, String layerName);

    FieldTypeNameWrapper getProperty(String entityName, String typeArgument, String instanceName);
}
