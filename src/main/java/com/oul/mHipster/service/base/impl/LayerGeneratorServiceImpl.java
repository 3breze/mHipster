package com.oul.mHipster.service.base.impl;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.oul.mHipster.service.ModelService;
import com.oul.mHipster.service.base.JavaFileMakerService;
import com.oul.mHipster.service.base.LayerGeneratorService;
import com.oul.mHipster.service.impl.ModelServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategyFactory;

public class LayerGeneratorServiceImpl implements LayerGeneratorService {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private GenerateLayerStrategyFactory generateLayerStrategyFactory;

    public LayerGeneratorServiceImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public void generateLayers(RootEntityModel rootEntityModel) {

        ModelService modelService = new ModelServiceImpl(layersConfig, rootEntityModel);
        LayerModelWrapper layerModelWrapper = modelService.initLayerModel();
        EntityManagerFactory.createEntityManager(layerModelWrapper);

        generateLayerStrategyFactory = new GenerateLayerStrategyFactory();
        rootEntityModel.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpecWrapper typeSpecWrapper = generateLayerStrategy.generate(entityModel);
            entityModel.getTypeSpecWrapperList().add(typeSpecWrapper);
        }));

        javaFileMakerService.makeJavaFiles(rootEntityModel);
    }
}
