package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.*;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

public class LayerBuilderServiceImpl implements LayerBuilderService {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public LayerBuilderServiceImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public void buildLayers(RootEntityModel rootEntityModel) {

        LayerModelService layerModelService = new LayerModelServiceImpl(layersConfig, rootEntityModel);
        LayerModelWrapper layerModelWrapper = layerModelService.initLayerModel();

        rootEntityModel.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layersMap = layerModelService.generateLayerClassNaming(entity, rootEntityModel);
            entity.setLayers(layersMap);
        });

        EntityManagerFactory entityManagerFactory = EntityManagerService.getInstance();
        entityManagerFactory.createEntityManager(rootEntityModel);

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
