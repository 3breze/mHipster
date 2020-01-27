package com.oul.mHipster.service.strategy.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.oul.mHipster.service.helper.JPoetHelperService;
import com.oul.mHipster.service.helper.impl.AttributeBuilderService;
import com.oul.mHipster.service.helper.impl.JPoetHelperServiceImpl;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;

public class GenerateResponseDtoClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;
    private AttributeBuilderService attributeBuilderService;

    public GenerateResponseDtoClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeBuilderService = new AttributeBuilderService();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        List<FieldSpec> fieldSpecList = attributeBuilderService.getFieldSpecList(entity, LayerName.RESPONSE_DTO.name());
        MethodSpec constructor = jPoetHelperService.buildConstructor(fieldSpecList, "");

//        List<MethodSpec> getterMethods = jPoetHelperService.buildGetters(entity.getAttributes());
//        List<MethodSpec> setterMethods = jPoetHelperService.buildSetters(entity.getAttributes());

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.RESPONSE_DTO.toString());
        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec
                        .builder(JsonInclude.class)
                        .addMember("value", "$L",
                                JsonInclude.Include.NON_NULL.name())
                        .build())
                .addFields(fieldSpecList)
                .addMethod(constructor)
//                .addMethods(getterMethods)
//                .addMethods(setterMethods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
