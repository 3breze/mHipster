package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.impl.EntityManagerService;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PoetHelperService {

    private EntityManagerFactory entityManagerFactory;

    public PoetHelperService() {
        this.entityManagerFactory = EntityManagerService.getInstance();
    }

    public MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getFieldName();
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

    //parametarizovano za serviceImpl i api layer-e
    public MethodSpec buildConstructor(Entity entity, String layerName) {

        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<ParameterSpec> parameterSpecsList = new ArrayList<>();

        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(), "daoClass");
        fieldSpecList.add(FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build());
        parameterSpecsList.add(ParameterSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .build());

        // izvuci iz abstraktne superklase?
        List<RelationAttribute> relationAttributes = entityManagerFactory.findRelationAttributes(entity);

        relationAttributes.forEach(attribute -> {

            FieldTypeNameWrapper typeNameWrapper = entityManagerFactory.getProperty(attribute.getClassSimpleName(), "serviceClass");

            fieldSpecList.add(FieldSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            parameterSpecsList.add(ParameterSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .build());
        });

        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(cb -> builder.addStatement("this.$N = $N", cb.name, cb.name));

        return MethodSpec.constructorBuilder()
                .addAnnotation(Autowired.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();
    }


}
