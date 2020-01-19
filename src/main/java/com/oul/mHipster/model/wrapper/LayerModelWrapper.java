package com.oul.mHipster.model.wrapper;

import java.util.Map;

public class LayerModelWrapper {

    private Map<String, Map<String, FieldTypeNameWrapper>> layerClassModel;

    public LayerModelWrapper(Map<String, Map<String, FieldTypeNameWrapper>> layerModel) {
        this.layerClassModel = layerModel;
    }

    public Map<String, Map<String, FieldTypeNameWrapper>> getLayerClassModel() {
        return layerClassModel;
    }

    public void setLayerClassModel(Map<String, Map<String, FieldTypeNameWrapper>> layerClassModel) {
        this.layerClassModel = layerClassModel;
    }
}
