package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class PoetHelperService {

    public MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = buildGetterName(fieldName);
        return MethodSpec.methodBuilder(getterName).returns(ClassName.bestGuess(attribute.getType().toString())).addModifiers(Modifier.PUBLIC).build();
    }

    private String buildGetterName(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }



    public CodeBlock buildFindByIdCodeBlock(Entity entity) {
        TypeName resourceNotFoundClass = ClassName.get("com.whatever.exception", "ResourceNotFoundException");
        return CodeBlock.builder()
                .addStatement("Optional<$T> $L = $L.findById(id)", entity.getClassName(),
                        entity.getOptionalName(), entity.getLayers().get(LayerName.DAO.toString()).getInstanceName())
                .beginControlFlow("if ($L.isEmpty())", entity.getOptionalName())
                .addStatement("throw new $T(\"$T $L\")", resourceNotFoundClass, resourceNotFoundClass, "not found!")
                .endControlFlow()
                .addStatement("$T $L = $L.get()", entity.getClassName(), entity.getInstanceName(), entity.getOptionalName())
                .build();
    }


}
