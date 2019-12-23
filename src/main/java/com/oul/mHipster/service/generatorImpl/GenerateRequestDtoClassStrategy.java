package com.oul.mHipster.service.generatorImpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GenerateRequestDtoClassStrategy implements GenerateLayerStrategy {

    @Override
    public TypeSpec generate(OldShitModel oldShitModel) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (Attribute attribute : oldShitModel.getEntity().getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType().toString()), attribute.getValue())
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
        String name = String.join("", oldShitModel.getEntity().getClassName(), suffix);
        return TypeSpec
                .classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(jsonNonNullAnno)
                .addFields(fieldSpecList)
                .addMethods(methodSpecList)
                .build();
    }
}
