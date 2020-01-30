package com.oul.mHipster.service.strategy.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.helper.JPoetHelperService;
import com.oul.mHipster.service.helper.impl.AttributeBuilderService;
import com.oul.mHipster.service.helper.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateResponseDtoClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;
    private AttributeBuilderService attributeBuilderService;

    public GenerateResponseDtoClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeBuilderService = new AttributeBuilderService();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        List<FieldSpec> attributeList = attributeBuilderService.getAttributeFieldSpecList(entity, LayerName.REQUEST_DTO.name());
        List<FieldSpec> relationAttributeList = attributeBuilderService.getRelationAttributeFieldSpecList(entity);
        List<FieldSpec> allAttributesList = Stream.concat(attributeList.stream(), relationAttributeList.stream())
                .collect(Collectors.toList());

        List<MethodSpec> getterMethods = jPoetHelperService.buildGetters(allAttributesList);
        List<MethodSpec> setterMethods = jPoetHelperService.buildSetters(allAttributesList);

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.RESPONSE_DTO.toString());
        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec
                        .builder(JsonInclude.class)
                        .addMember("value", "$L",
                                JsonInclude.Include.NON_NULL.name())
                        .build())
                .addFields(attributeList)
                .addFields(relationAttributeList)
                .addMethods(getterMethods)
                .addMethods(setterMethods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
