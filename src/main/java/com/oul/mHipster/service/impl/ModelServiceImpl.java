package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.ModelService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelServiceImpl implements ModelService {

    private LayersConfig layersConfig;
    private RootEntityModel rootEntityModel;
    private Map<String, Map<String, FieldTypeNameWrapper>> layerModel = new HashMap<>();

    public ModelServiceImpl(LayersConfig layersConfig, RootEntityModel rootEntityModel) {
        this.layersConfig = layersConfig;
        this.rootEntityModel = rootEntityModel;
    }

    public LayerModelWrapper initLayerModel() {
        generateLayerClassNaming();
        registerDependenciesTypeNames();
        registerDomainLayerTypeNames();
        return new LayerModelWrapper(layerModel);
    }

    public void generateLayerClassNaming() {
        rootEntityModel.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layersMap = createLayerClassNaming(entity, rootEntityModel);
            entity.setLayers(layersMap);
        });
    }

    private Map<String, ClassNamingInfo> createLayerClassNaming(Entity entity, RootEntityModel rootEntityModel) {
        return layersConfig.getLayers().stream().collect(Collectors.toMap(Layer::getName, layer -> {
            String className = entity.getClassName() + layer.getNamingSuffix();
            String instanceName = ClassUtils.instanceNameBuilder(className);
            String packageName = rootEntityModel.getRootPackageName() + "." + layer.getPackageName();
            return new ClassNamingInfo(className, instanceName, packageName);
        }));
    }

    private void registerDependenciesTypeNames() {
        Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

        typeNameMap.put("Pageable", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Pageable"), "pageable"));
        typeNameMap.put("Page", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Page"), "page"));
        typeNameMap.put("PageImpl", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "PageImpl"), "pageImpl"));
        typeNameMap.put("Predicate", new FieldTypeNameWrapper(
                ClassName.get("com.querydsl.core.types", "Predicate"), "predicate"));
        typeNameMap.put("ResourceNotFoundException", new FieldTypeNameWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".exceptions", "ResourceNotFoundException"),
                "ResourceNotFoundException"));
        typeNameMap.put("ValidationGroup", new FieldTypeNameWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".property", "ValidationGroup"),
                "ValidationGroup"));
        typeNameMap.put("ValidationGroupUpdate", new FieldTypeNameWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".ValidationGroup.property", "Update"),
                "Update"));
        typeNameMap.put("ValidationGroupSave", new FieldTypeNameWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".ValidationGroup.property", "Save"),
                "Save"));
        typeNameMap.put("JpaRepository", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.jpa.repository", "JpaRepository"),
                "JpaRepository"));
        typeNameMap.put("QuerydslPredicateExecutor", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.querydsl", "QuerydslPredicateExecutor"),
                "QuerydslPredicateExecutor"));

        layerModel.put("dependencies", typeNameMap);
    }


    private void registerDomainLayerTypeNames() {
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
