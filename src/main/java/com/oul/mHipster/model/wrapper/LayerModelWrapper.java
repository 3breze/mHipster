package com.oul.mHipster.model.wrapper;

import java.util.HashMap;
import java.util.Map;

public class LayerModelWrapper {

    private Map<String, Map<String, FieldTypeNameWrapper>> layerClassModel = new HashMap<>();

    public LayerModelWrapper(Map<String, Map<String, FieldTypeNameWrapper>> layerModel) {
    }

    public Map<String, Map<String, FieldTypeNameWrapper>> getLayerClassModel() {
        return layerClassModel;
    }

    public void setLayerClassModel(Map<String, Map<String, FieldTypeNameWrapper>> layerClassModel) {
        this.layerClassModel = layerClassModel;
    }
}
