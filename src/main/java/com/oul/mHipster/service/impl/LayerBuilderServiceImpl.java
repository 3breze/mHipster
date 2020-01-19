package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.*;
import com.squareup.javapoet.TypeSpec;

public class LayerBuilderServiceImpl implements LayerBuilderService {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private GenerateLayerStrategyFactory generateLayerStrategyFactory;

    public LayerBuilderServiceImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public void buildLayers(RootEntityModel rootEntityModel) {

        LayerModelService layerModelService = new LayerModelServiceImpl(layersConfig, rootEntityModel);
        LayerModelWrapper layerModelWrapper = layerModelService.initLayerModel();
        EntityManagerFactory.createEntityManager(layerModelWrapper);

        generateLayerStrategyFactory = new GenerateLayerStrategyFactory();
        rootEntityModel.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            // Potrebne izmene jer ce jedan entity imati niz TypeSpecova
            // Izmene trebaju i u javaFileMakerService gde se setuje packageName
            if (layer.getName().equals(LayerName.SERVICE_IMPL.toString()))
                entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(rootEntityModel);
    }
}
