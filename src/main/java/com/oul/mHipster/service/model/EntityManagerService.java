package com.oul.mHipster.service.model;

import com.oul.mHipster.layerconfig.wrapper.CodeBlockStatement;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;

import java.util.List;
import java.util.Map;

public interface EntityManagerService {

    void setLayerModelFactory(LayerModelWrapper layerModelWrapper);

    TypeWrapper getProperty(String entityName, String layerName);

    TypeWrapper getProperty(String entityName, String typeArgument, String instanceName);

    CodeBlockStatement computeStatement(String helperName, Integer statementIdx, Map<String, String> classNamesMap);

}
