package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.generatorImpl.*;
//final
public class GenerateLayerStrategyFactory {

    private final GenerateRequestDtoClassStrategy generateRequestDtoClassStrategy = new GenerateRequestDtoClassStrategy();
    private final GenerateResponseDtoClassStrategy generateResponseDtoClassStrategy = new GenerateResponseDtoClassStrategy();
    private final GenerateApiClassStrategy generateApiClassStrategy = new GenerateApiClassStrategy();
    private final GenerateServiceClassStrategy generateServiceClassStrategy = new GenerateServiceClassStrategy();
    private final GenerateServiceImplClassStrategy generateServiceImplClassStrategy = new GenerateServiceImplClassStrategy();
    private final GenerateDaoClassStrategy generateDaoClassStrategy = new GenerateDaoClassStrategy();
    private final UnknownLayerStrategy unknownLayerStrategy = new UnknownLayerStrategy();


    GenerateLayerStrategy getLayerStrategy(LayerName layerName) {
        switch (layerName) {
            case REQUEST_DTO: return generateRequestDtoClassStrategy;
            case RESPONSE_DTO: return generateResponseDtoClassStrategy;
            case API: return generateApiClassStrategy;
            case SERVICE: return generateServiceClassStrategy;
            case SERVICE_IMPL: return generateServiceImplClassStrategy;
            case DAO: return generateDaoClassStrategy;
            default: return unknownLayerStrategy;
        }
    }
}
