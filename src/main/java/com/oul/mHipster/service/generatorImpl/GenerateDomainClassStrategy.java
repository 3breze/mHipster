package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GenerateDomainClassStrategy implements GenerateLayerStrategy {

    @Override
    public TypeSpec generate(OldShitModel oldShitModel) {
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Attribute attribute : oldShitModel.getEntity().getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess(attribute.getType()), attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
        }
        return TypeSpec
                .classBuilder(oldShitModel.getEntity().getClassName())
                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Getter.class)
//                .addAnnotation(Setter.class)
//                .addAnnotation(NoArgsConstructor.class)
                .addFields(fieldSpecList)
                .build();
    }
}
