package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpec generate(OldShitModel oldShitModel) {
        oldShitModel.getEntity().getClassName();

        TypeSpec reqDtoClass = TypeSpec.classBuilder(oldShitModel.getRequestDtoClassName()).build();
        TypeName rere = ClassName.get("com.whatever.whatever", oldShitModel.getRequestDtoClassName());

        MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC).returns(rere).build();
        return null;
    }
}
