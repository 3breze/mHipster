package com.oul.mHipster.service;

import com.oul.mHipster.util.Util;
import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.SourceDomainLayer;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oul.mHipster.layersConfig.enums.LayerName.*;

public class EntityBuilderService {

    private SourceDomainLayer sourceDomainLayer;
    private JavaFileMakerService javaFileMakerService;
    private LayersConfig layersConfig;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityBuilderService(SourceDomainLayer sourceDomainLayer, LayersConfig layersConfig) {
        this.sourceDomainLayer = sourceDomainLayer;
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public void buildEntityModel() {
        Map<String, Layer> domainSpecificLayersMap = layersConfig.getLayers().stream()
//                .filter(layer -> Arrays.asList(DOMAIN, RESPONSE_DTO, REQUEST_DTO).contains(LayerName.valueOf(layer.getName())))
                .collect(Collectors.toMap(Layer::getName, layer -> layer));

        List<OldShitModel> oldShitModelList = sourceDomainLayer.getEntities().stream().map(entity ->
                new OldShitModel.EntityModelBuilder().classAndInstanceName(entity.getClassName())
                        .requestClassAndInstanceName(domainSpecificLayersMap.get(REQUEST_DTO).getNamingSuffix())
                        .responseClassAndInstanceName(domainSpecificLayersMap.get(RESPONSE_DTO).getNamingSuffix())
                        .packageName(Util.getValue(DOMAIN))
                        .entity(entity)
                        .build())
                .collect(Collectors.toList());

        oldShitModelList.forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(oldShitModelList);
    }

}
