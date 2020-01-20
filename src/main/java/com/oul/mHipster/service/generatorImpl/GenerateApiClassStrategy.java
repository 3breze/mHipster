package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.web.bind.annotation.RequestMapping;

public class GenerateApiClassStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpec generate(Entity entity) {
        TypeSpec spec = TypeSpec.classBuilder("SomeResource")
                .addAnnotation(
                        AnnotationSpec.builder(RequestMapping.class)
                                .addMember("value", "$S", "/api")
                                .build())
                .build();
        return null;
    }
}
