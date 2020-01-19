package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.LayerModelService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LayerModelServiceImpl implements LayerModelService {

    private LayersConfig layersConfig;
    private RootEntityModel rootEntityModel;
    private Map<String, Map<String, FieldTypeNameWrapper>> layerModel = new HashMap<>();

    LayerModelServiceImpl(LayersConfig layersConfig, RootEntityModel rootEntityModel) {
        this.layersConfig = layersConfig;
        this.rootEntityModel = rootEntityModel;
    }

    public LayerModelWrapper initLayerModel() {
        createDependenciesTypeNames();
        createRelationTypeNames();
        return new LayerModelWrapper(layerModel);
    }

    @Override
    public Map<String, ClassNamingInfo> generateLayerClassNaming(Entity entity, RootEntityModel rootEntityModel) {
        return layersConfig.getLayers().stream().collect(Collectors.toMap(Layer::getName, layer -> {
            String className = entity.getClassName() + layer.getNamingSuffix();
            String instanceName = ClassUtils.instanceNameBuilder(className);
            String packageName = rootEntityModel.getRootPackageName() + "." + layer.getPackageName();
            return new ClassNamingInfo(className, instanceName, packageName);
        }));
    }

    private void createDependenciesTypeNames() {
        Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

        typeNameMap.put("Pageable", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Pageable"), "pageable"));
        typeNameMap.put("Page", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Page"), "page"));
        typeNameMap.put("PageImpl", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "PageImpl"), "pageImpl"));
        typeNameMap.put("Predicate", new FieldTypeNameWrapper(
                ClassName.get("import com.querydsl.core.types", "Predicate"), "predicate"));

        layerModel.put("dependencies", typeNameMap);
    }


    private void createRelationTypeNames() {
        this.rootEntityModel.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layerMap = entity.getLayers();
            Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

            typeNameMap.put("domainClass", new FieldTypeNameWrapper(
                    ClassName.get(entity.getPackageName(), entity.getClassName()), entity.getInstanceName()));

            ClassNamingInfo requestInfo = layerMap.get(LayerName.REQUEST_DTO.toString());
            typeNameMap.put("requestClass", new FieldTypeNameWrapper(
                    ClassName.get(requestInfo.getPackageName(), requestInfo.getClassName()), requestInfo.getInstanceName()));

            ClassNamingInfo responseInfo = layerMap.get(LayerName.RESPONSE_DTO.toString());
            typeNameMap.put("responseClass", new FieldTypeNameWrapper(
                    ClassName.get(responseInfo.getPackageName(), responseInfo.getClassName()), responseInfo.getInstanceName()));

            ClassNamingInfo daoInfo = layerMap.get(LayerName.DAO.toString());
            typeNameMap.put("daoClass", new FieldTypeNameWrapper(
                    ClassName.get(daoInfo.getPackageName(), daoInfo.getClassName()), daoInfo.getInstanceName()));

            ClassNamingInfo apiInfo = layerMap.get(LayerName.API.toString());
            typeNameMap.put("apiClass", new FieldTypeNameWrapper(
                    ClassName.get(apiInfo.getPackageName(), apiInfo.getClassName()), apiInfo.getInstanceName()));

            ClassNamingInfo serviceInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceClass", new FieldTypeNameWrapper(
                    ClassName.get(serviceInfo.getPackageName(), serviceInfo.getClassName()), serviceInfo.getInstanceName()));

            ClassNamingInfo serviceImplInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceImplClass", new FieldTypeNameWrapper(
                    ClassName.get(serviceImplInfo.getPackageName(), serviceImplInfo.getClassName()), serviceImplInfo.getInstanceName()));

            layerModel.put(entity.getClassName(), typeNameMap);
        });
    }
}
