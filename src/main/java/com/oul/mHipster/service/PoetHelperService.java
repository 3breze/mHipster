package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        typeNameMap.put("requestClazz", ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
                layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()));
        typeNameMap.put("responseClazz", ClassName.get(layerMap.get(LayerName.RESPONSE_DTO.toString()).getPackageName(),
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

    public MethodSpec buildConstructor(Entity entity) {
        Map<String, LayerClass> layerMap = entity.getLayers();
        Map<String, TypeName> typeNameMap = createTypeNames(entity);
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<ParameterSpec> parameterSpecsList = new ArrayList<>();

        entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                        ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .forEach(attribute -> {
                    fieldSpecList.add(FieldSpec
                            .builder(typeNameMap.get("serviceImplClass"), layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                            .addModifiers(Modifier.PRIVATE)
                            .build());
                    parameterSpecsList.add(ParameterSpec
                            .builder(typeNameMap.get("serviceImplClass"), layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                            .build());
                });

        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(cb -> builder.addStatement("this.$N = $N", cb, cb));

        return MethodSpec.constructorBuilder()
                .addAnnotation(Autowired.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();
    }


}
