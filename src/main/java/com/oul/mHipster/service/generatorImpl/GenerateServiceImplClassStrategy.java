package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.*;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpec generate(Entity entity) {
//        entity.getClassName();
//
//        TypeSpec reqDtoClass = TypeSpec.classBuilder(oldShitModel.getRequestDtoClassName()).build();
//        TypeName rere = ClassName.get("com.whatever.whatever", oldShitModel.getRequestDtoClassName());
//
//        MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC).returns(rere).build();
        return null;
    }
}
