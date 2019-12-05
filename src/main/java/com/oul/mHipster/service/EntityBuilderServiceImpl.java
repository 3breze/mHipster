package com.oul.mHipster.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.domainApp.Attribute;
import com.oul.mHipster.domainApp.Entity;
import com.oul.mHipster.domainApp.EntityBuilderConfig;
import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class EntityBuilderServiceImpl {

    private EntityBuilderConfig entityBuilderConfig;
    private JavaFileMakerService javaFileMakerService;
    private LayerGeneratorService layerGeneratorService;

    public EntityBuilderServiceImpl(EntityBuilderConfig entityBuilderConfig, JavaFileMakerService javaFileMakerService) {
        this.entityBuilderConfig = entityBuilderConfig;
        this.javaFileMakerService = javaFileMakerService;
    }

    public void buildEntity() {
        for (Entity entity : entityBuilderConfig.getEntities()) {
            TypeSpec typeSpec = writeDomainClass(entity);
//            List<JavaFile> javaFileList = layerItUp(typeSpec);
//            javaFileMakerService.makeJavaFiles(javaFileList);
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
                .addAnnotation(Getter.class)
                .addAnnotation(Setter.class)
                .addAnnotation(NoArgsConstructor.class)
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
            MethodSpec getterMethodSpec = buildGetter(attribute);
            methodSpecList.add(getterMethodSpec);
        }
        AnnotationSpec jsonNonNullAnno = AnnotationSpec
                .builder(JsonInclude.class)
                .addMember("value", "JsonInclude.Include.NON_NULL")
                .build();
        return TypeSpec
                .classBuilder(entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(jsonNonNullAnno)
                .addFields(fieldSpecList)
                .addMethods(methodSpecList)
                .build();
    }

    private MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).toLowerCase();
        String packageName = String.join(".", entityBuilderConfig.getGroupName(), entityBuilderConfig.getArtifactName(), "domain");
        TypeName retType = ClassName.get(packageName, fieldName);
        return MethodSpec.methodBuilder(getterName).returns(retType).build();
    }
}
