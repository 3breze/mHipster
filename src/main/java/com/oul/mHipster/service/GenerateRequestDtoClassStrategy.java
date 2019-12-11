package com.oul.mHipster.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domainApp.Attribute;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GenerateRequestDtoClassStrategy implements GenerateLayerStrategy {

    @Override
    public TypeSpec generate(EntityModel entityModel) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (Attribute attribute : entityModel.getEntity().getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType()), attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
//            MethodSpec getterMethodSpec = poetHelperService.buildGetter(attribute);
//            methodSpecList.add(getterMethodSpec);
        }
        AnnotationSpec jsonNonNullAnno = AnnotationSpec
                .builder(JsonInclude.class)
                .addMember("value", "JsonInclude.Include.NON_NULL")
                .build();
        String suffix = "RequestDto";
        String name = String.join("", entityModel.getEntity().getName(), suffix);
        return TypeSpec
                .classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(jsonNonNullAnno)
                .addFields(fieldSpecList)
                .addMethods(methodSpecList)
                .build();
    }
}
