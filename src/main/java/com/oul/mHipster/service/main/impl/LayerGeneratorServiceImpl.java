package com.oul.mHipster.service.main.impl;

import com.oul.mHipster.layerconfig.LayersConfig;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.main.JavaFileMakerService;
import com.oul.mHipster.service.main.LayerGeneratorService;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.ModelService;
import com.oul.mHipster.service.model.impl.ModelServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.oul.mHipster.service.strategy.GenerateLayerStrategyFactory;

import java.util.List;

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

        List<TypeSpecWrapper> sharedClasses = modelService.generateSharedClasses();
        rootEntityModel.setSharedClasses(sharedClasses);

        javaFileMakerService.makeJavaFiles(rootEntityModel);
    }
}
