package com.oul.mHipster.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domainApp.Attribute;
import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainApp.Entity;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityBuilderService {

    private EntitiesConfig entitiesConfig;
    private PoetHelperService poetHelperService;
    private JavaFileMakerService javaFileMakerService;
    private LayerGeneratorService layerGeneratorService;
    private LayersConfig layersConfig;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityBuilderService(EntitiesConfig entitiesConfig, LayersConfig layersConfig) {
        this.entitiesConfig = entitiesConfig;
        this.layersConfig = layersConfig;
        this.poetHelperService = new PoetHelperService();
        this.layerGeneratorService = new LayerGeneratorService(layersConfig);
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public void buildEntityModel() {

        //Kreiranje Liste EntityModela
        // sibanje jedno po jednog u loop po layerima koji se pusta u Factory
        //strategije su formata writeDomainClass

        Layer dtoResponseLayer = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("domain.dto.response")).findAny().orElse(null);
        Layer dtoRequestLayer = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("domain.dto.request")).findAny().orElse(null);

        List<EntityModel> entityModelList = entitiesConfig.getEntities().stream().map(entity ->
                new EntityModel.EntityModelBuilder().classAndInstanceName(entity.getName())
                        .requestClassAndInstanceName(dtoRequestLayer.getNamingSuffix())
                        .responseClassAndInstanceName(dtoResponseLayer.getNamingSuffix())
                        .build())
                .collect(Collectors.toList());

        entityModelList.forEach(entityModel -> layersConfig.getLayers().forEach(layer -> generateLayerStrategyFactory.getLayerStrategy(layer.getName(), entityModel)));
        for (Entity entity : entitiesConfig.getEntities()) {
            entityModelList.addAll(layerGeneratorService.generateLayers(entity));
            javaFileMakerService.makeJavaFiles(entityModelList);
        }

    }


    private TypeSpec writeDomainClass(Entity entity) {

        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Attribute attribute : entity.getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType()), attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
        }
        return TypeSpec
                .classBuilder(entity.getName())
                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Getter.class)
//                .addAnnotation(Setter.class)
//                .addAnnotation(NoArgsConstructor.class)
                .addFields(fieldSpecList)
                .build();
    }

    private TypeSpec writeDtoClass(Entity entity) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (Attribute attribute : entity.getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType()), attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            MethodSpec getterMethodSpec = poetHelperService.buildGetter(attribute);
            methodSpecList.add(getterMethodSpec);
        }
        AnnotationSpec jsonNonNullAnno = AnnotationSpec
                .builder(JsonInclude.class)
                .addMember("value", "JsonInclude.Include.NON_NULL")
                .build();
        String suffix = "RequestDto";
        String name = String.join("", entity.getName(), suffix);
        return TypeSpec
                .classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(jsonNonNullAnno)
                .addFields(fieldSpecList)
                .addMethods(methodSpecList)
                .build();
    }


}
