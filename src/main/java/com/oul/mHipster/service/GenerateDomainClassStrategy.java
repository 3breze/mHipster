package com.oul.mHipster.service;

import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domainApp.Attribute;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GenerateDomainClassStrategy implements GenerateLayerStrategy {

    @Override
    public TypeSpec generate(EntityModel entityModel) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Attribute attribute : entityModel.getEntity().getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType()), attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
        }
        return TypeSpec
                .classBuilder(entityModel.getEntity().getName())
                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Getter.class)
//                .addAnnotation(Setter.class)
//                .addAnnotation(NoArgsConstructor.class)
                .addFields(fieldSpecList)
                .build();
    }
}
