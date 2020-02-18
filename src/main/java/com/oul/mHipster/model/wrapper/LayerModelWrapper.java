package com.oul.mHipster.model.wrapper;

import com.oul.mHipster.layerconfig.wrapper.CodeBlockStatement;

import java.util.Map;

public class LayerModelWrapper {

    private Map<String, Map<String, TypeWrapper>> layerClassModelMap;
    private Map<String, Map<Integer, CodeBlockStatement>> methodStatementMap;

    public LayerModelWrapper(Map<String, Map<String, TypeWrapper>> layerModel, Map<String, Map<Integer, CodeBlockStatement>> methodStatementMap) {
        this.layerClassModelMap = layerModel;
        this.methodStatementMap = methodStatementMap;
    }

    public Map<String, Map<String, TypeWrapper>> getLayerClassModelMap() {
        return layerClassModelMap;
    }

    public Map<String, Map<Integer, CodeBlockStatement>> getMethodStatementMap() {
        return methodStatementMap;
    }
}
