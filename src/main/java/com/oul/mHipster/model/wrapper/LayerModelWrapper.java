package com.oul.mHipster.model.wrapper;

import java.util.Map;

public class LayerModelWrapper {

    private Map<String, Map<String, TypeWrapper>> layerClassModel;

    public LayerModelWrapper(Map<String, Map<String, TypeWrapper>> layerModel) {
        this.layerClassModel = layerModel;
    }

    public Map<String, Map<String, TypeWrapper>> getLayerClassModel() {
        return layerClassModel;
    }

    public void setLayerClassModel(Map<String, Map<String, TypeWrapper>> layerClassModel) {
        this.layerClassModel = layerClassModel;
    }
}
