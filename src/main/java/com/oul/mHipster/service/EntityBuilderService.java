package com.oul.mHipster.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.Util;
import com.oul.mHipster.domain.TypeSpecWrapper;
import com.oul.mHipster.domainApp.Attribute;
import com.oul.mHipster.domainApp.Entity;
import com.oul.mHipster.domainApp.EntityBuilderConfig;
import com.oul.mHipster.domainConfig.LayersConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class EntityBuilderService {

    private EntityBuilderConfig entityBuilderConfig;
    private PoetHelperService poetHelperService;
    private JavaFileMakerService javaFileMakerService;
    private LayerGeneratorService layerGeneratorService;
    private LayersConfig layersConfig;

    public EntityBuilderService(EntityBuilderConfig entityBuilderConfig, LayersConfig layersConfig) {
        this.entityBuilderConfig = entityBuilderConfig;
        this.layersConfig = layersConfig;
        this.poetHelperService = new PoetHelperService();
        this.layerGeneratorService = new LayerGeneratorService(layersConfig);
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public void buildEntity() {
        List<TypeSpecWrapper> typeSpecWrapperList = new ArrayList<>();
        for (Entity entity : entityBuilderConfig.getEntities()) {
            TypeSpec domainClass = writeDomainClass(entity);
            TypeSpec dtoClass = writeDtoClass(entity);
            typeSpecWrapperList.add(new TypeSpecWrapper(domainClass, "domain"));
            typeSpecWrapperList.add(new TypeSpecWrapper(dtoClass, "domain.dto.request"));
            typeSpecWrapperList.addAll(layerGeneratorService.generateLayers(entity));
            javaFileMakerService.makeJavaFiles(typeSpecWrapperList);
        }

    }


    private TypeSpec writeDomainClass(Entity entity) {

        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Attribute attribute : entity.getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(String.class, attribute.getValue())
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
                    .builder(String.class, attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            MethodSpec getterMethodSpec = poetHelperService.buildGetter(attribute);
            methodSpecList.add(getterMethodSpec);
        }
        AnnotationSpec jsonNonNullAnno = AnnotationSpec
                .builder(JsonInclude.class)
                .addMember("value", "JsonInclude.Include.NON_NULL")
                .build();
        String suffix = Util.getValue("domain.dto.request");
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
