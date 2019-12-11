package com.oul.mHipster.service;

import com.oul.mHipster.Util;
import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domain.LayerName;
import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oul.mHipster.domain.LayerName.*;

public class EntityBuilderService {

    private EntitiesConfig entitiesConfig;
    private JavaFileMakerService javaFileMakerService;
    private LayersConfig layersConfig;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityBuilderService(EntitiesConfig entitiesConfig, LayersConfig layersConfig) {
        this.entitiesConfig = entitiesConfig;
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public void buildEntityModel() {
        Map<String, Layer> domainSpecificLayersMap = layersConfig.getLayers().stream()
                .filter(layer -> Arrays.asList(DOMAIN, RESPONSE_DTO, REQUEST_DTO).contains(LayerName.valueOf(layer.getName())))
                .collect(Collectors.toMap(Layer::getName, layer -> layer));

        List<EntityModel> entityModelList = entitiesConfig.getEntities().stream().map(entity ->
                new EntityModel.EntityModelBuilder().classAndInstanceName(entity.getName())
                        .requestClassAndInstanceName(domainSpecificLayersMap.get(REQUEST_DTO).getNamingSuffix())
                        .responseClassAndInstanceName(domainSpecificLayersMap.get(RESPONSE_DTO).getNamingSuffix())
                        .packageName(Util.getValue(DOMAIN))
                        .entity(entity)
                        .build())
                .collect(Collectors.toList());

        entityModelList.forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(entityModelList);
    }

}
