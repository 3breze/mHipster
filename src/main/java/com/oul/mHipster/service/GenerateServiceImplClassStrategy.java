package com.oul.mHipster.service;

import com.oul.mHipster.domain.EntityModel;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpec generate(EntityModel entityModel) {
        entityModel.getEntity().getName();

        TypeSpec reqDtoClass = TypeSpec.classBuilder(entityModel.getRequestDtoClassName()).build();
        TypeName rere = ClassName.get("com.whatever.whatever", entityModel.getRequestDtoClassName());

        MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC).returns(rere).build();
        return null;
    }
}
