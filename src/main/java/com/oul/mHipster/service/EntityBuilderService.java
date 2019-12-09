package com.oul.mHipster.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domainApp.Attribute;
import com.oul.mHipster.domainApp.Entity;
import com.oul.mHipster.domainApp.EntitiesConfig;
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

    public EntityBuilderService(EntitiesConfig entitiesConfig, LayersConfig layersConfig) {
        this.entitiesConfig = entitiesConfig;
        this.layersConfig = layersConfig;
        this.poetHelperService = new PoetHelperService();
        this.layerGeneratorService = new LayerGeneratorService(layersConfig);
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public void buildEntity() {

        //Kreiranje Liste EntityModela
        // sibanje jedno po jednog u loop po layerima koji se pusta u Factory
        //strategije su formata writeDomainClass

        List<EntityModel> entityModelList = entitiesConfig.getEntities().stream().map(entity -> {
            EntityModel entityModel = new EntityModel();
            entityModel.setClassName(entity.getName());
            String fieldName = entity.getName();

            fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            entityModel.setInstanceName(entity.getName());
        }).collect(Collectors.toList());

        List<EntityModel> entityModelList = new ArrayList<>();
        for (Entity entity : entitiesConfig.getEntities()) {
            TypeSpec domainClass = writeDomainClass(entity);
            TypeSpec dtoClass = writeDtoClass(entity);
            entityModelList.add(new EntityModel(domainClass, "domain"));
            entityModelList.add(new EntityModel(dtoClass, "domain.dto.request"));
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
