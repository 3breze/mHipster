package com.oul.mHipster.service.model.impl;

import com.oul.mHipster.layerconfig.Layer;
import com.oul.mHipster.layerconfig.LayersConfig;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.ModelService;
import com.oul.mHipster.service.poetic.JPoetClassService;
import com.oul.mHipster.service.poetic.impl.JPoetClassServiceImpl;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelServiceImpl implements ModelService {

    private LayersConfig layersConfig;
    private RootEntityModel rootEntityModel;
    private Map<String, Map<String, TypeWrapper>> layerModel = new HashMap<>();

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
        Map<String, TypeWrapper> typeNameMap = new HashMap<>();

        layersConfig.getDependencies().forEach(dependency ->
                typeNameMap.put(dependency.getSimpleName(),
                        new TypeWrapper(ClassName.get(dependency.getPackageName(), dependency.getSimpleName()), dependency.getSimpleName().toLowerCase()))
        );

        typeNameMap.put("ResourceNotFoundException", new TypeWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".shared.exception.specification", "ResourceNotFoundException"),
                "ResourceNotFoundException"));
        typeNameMap.put("ValidationGroup", new TypeWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".shared.property", "ValidationGroup"),
                "ValidationGroup"));
        typeNameMap.put("ValidationGroupUpdate", new TypeWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".shared.property.ValidationGroup", "Update"),
                "Update"));
        typeNameMap.put("ValidationGroupSave", new TypeWrapper(
                ClassName.get(rootEntityModel.getRootPackageName() + ".shared.property.ValidationGroup", "Save"),
                "Save"));

        layerModel.put("dependencies", typeNameMap);
    }


    private void registerDomainLayerTypeNames() {
        this.rootEntityModel.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layerMap = entity.getLayers();
            Map<String, TypeWrapper> typeNameMap = new HashMap<>();

            typeNameMap.put("domainClass", new TypeWrapper(
                    ClassName.get(entity.getPackageName(), entity.getClassName()), entity.getInstanceName()));

            ClassNamingInfo requestInfo = layerMap.get(LayerName.REQUEST_DTO.toString());
            typeNameMap.put("requestClass", new TypeWrapper(
                    ClassName.get(requestInfo.getPackageName(), requestInfo.getClassName()), requestInfo.getInstanceName()));

            ClassNamingInfo responseInfo = layerMap.get(LayerName.RESPONSE_DTO.toString());
            typeNameMap.put("responseClass", new TypeWrapper(
                    ClassName.get(responseInfo.getPackageName(), responseInfo.getClassName()), responseInfo.getInstanceName()));

            ClassNamingInfo daoInfo = layerMap.get(LayerName.DAO.toString());
            typeNameMap.put("daoClass", new TypeWrapper(
                    ClassName.get(daoInfo.getPackageName(), daoInfo.getClassName()), daoInfo.getInstanceName()));

            ClassNamingInfo apiInfo = layerMap.get(LayerName.API.toString());
            typeNameMap.put("apiClass", new TypeWrapper(
                    ClassName.get(apiInfo.getPackageName(), apiInfo.getClassName()), apiInfo.getInstanceName()));

            ClassNamingInfo serviceInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceClass", new TypeWrapper(
                    ClassName.get(serviceInfo.getPackageName(), serviceInfo.getClassName()), serviceInfo.getInstanceName()));

            ClassNamingInfo serviceImplInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceImplClass", new TypeWrapper(
                    ClassName.get(serviceImplInfo.getPackageName(), serviceImplInfo.getClassName()), serviceImplInfo.getInstanceName()));

            layerModel.put(entity.getClassName(), typeNameMap);
        });
    }

    public List<TypeSpecWrapper> generateSharedClasses() {
        List<TypeSpecWrapper> sharedClasses = new ArrayList<>();

        JPoetClassService jPoetClassService = new JPoetClassServiceImpl();
        TypeSpec exceptionTypeSpec = jPoetClassService.buildResourceNotFoundException();
        sharedClasses.add(new TypeSpecWrapper(exceptionTypeSpec, rootEntityModel.getRootPackageName() + ".shared.exception.specification"));

        TypeSpec validationTypeSpec = jPoetClassService.buildValidationGroup();
        sharedClasses.add(new TypeSpecWrapper(validationTypeSpec, rootEntityModel.getRootPackageName() + ".shared.property"));
        return sharedClasses;
    }
}
