package com.oul.mHipster.service.strategy.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.helper.JPoetHelperService;
import com.oul.mHipster.service.helper.impl.JPoetHelperServiceImpl;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateRequestDtoClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;

    public GenerateRequestDtoClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

//        List<FieldSpec> fieldSpecList = entity.getAttributes().stream().map(attribute -> FieldSpec
//                .builder(ClassName.bestGuess(attribute.getType().toString()), attribute.getFieldName())
//                .addModifiers(Modifier.PRIVATE)
//                .build()).collect(Collectors.toList());
//        MethodSpec constructor = jPoetHelperService.buildConstructor(fieldSpecList, null);
//
//        List<MethodSpec> getterMethods = jPoetHelperService.buildGetters(entity.getAttributes());
//        List<MethodSpec> setterMethods = jPoetHelperService.buildSetters(entity.getAttributes());

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.REQUEST_DTO.toString());
        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
//                .addFields(fieldSpecList)
//                .addMethod(constructor)
//                .addMethods(getterMethods)
//                .addMethods(setterMethods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
