package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

public class PoetHelperService {

    public MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = buildGetterName(fieldName);
        return MethodSpec.methodBuilder(getterName).returns(ClassName.bestGuess(attribute.getType().toString())).addModifiers(Modifier.PUBLIC).build();
    }

    private String buildGetterName(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public Map<String, TypeName> createTypeNames(Entity entity) {
        Map<String, LayerClass> layerMap = entity.getLayers();
        Map<String, TypeName> typeNameMap = new HashMap<>();

        typeNameMap.put("domainClass", ClassName.get(entity.getPackageName(), entity.getClassName()));
        typeNameMap.put("requestDtoClass", ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
                layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()));
        typeNameMap.put("responseDtoClass", ClassName.get(layerMap.get(LayerName.RESPONSE_DTO.toString()).getPackageName(),
                layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()));
        typeNameMap.put("daoClass", ClassName.get(layerMap.get(LayerName.DAO.toString()).getPackageName(),
                layerMap.get(LayerName.DAO.toString()).getClassName()));
        typeNameMap.put("apiClass", ClassName.get(layerMap.get(LayerName.API.toString()).getPackageName(),
                layerMap.get(LayerName.API.toString()).getClassName()));
        typeNameMap.put("serviceImplClass", ClassName.get(layerMap.get(LayerName.SERVICE_IMPL.toString()).getPackageName(),
                layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName()));
        return typeNameMap;
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
