package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.service.poetic.impl.AttributeService;
import com.oul.mHipster.service.poetic.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateRequestDtoClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;
    private AttributeService attributeService;

    public GenerateRequestDtoClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        List<FieldSpec> attributeList = attributeService.getAttributeFieldSpecList(entity, LayerName.REQUEST_DTO.name());
        List<FieldSpec> relationAttributeList = attributeService.getRelationAttributeFieldSpecList(entity, LayerName.REQUEST_DTO);
        List<FieldSpec> allAttributesList = Stream.concat(attributeList.stream(), relationAttributeList.stream())
                .collect(Collectors.toList());

        List<MethodSpec> getterMethods = jPoetHelperService.buildGetters(allAttributesList);
        List<MethodSpec> setterMethods = jPoetHelperService.buildSetters(allAttributesList);

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.REQUEST_DTO.toString());

        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addFields(attributeList)
                .addFields(relationAttributeList)
                .addMethods(getterMethods)
                .addMethods(setterMethods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
