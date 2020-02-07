package com.oul.mHipster.service.model;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;

import java.util.List;

public interface EntityManagerService {

    void setLayerModel(LayerModelWrapper layerModelWrapper);

    TypeWrapper getProperty(String entityName, String layerName);

    TypeWrapper getProperty(String entityName, String typeArgument, String instanceName);

    Object[] processStatementArgs(String entityName, List<String> statementArgs);

}
