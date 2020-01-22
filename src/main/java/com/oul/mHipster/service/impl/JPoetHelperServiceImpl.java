package com.oul.mHipster.service.impl;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.JPoetHelperService;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JPoetHelperServiceImpl implements JPoetHelperService {

    private EntityManagerService entityManagerService;
    private AttributeService attributeService;

    public JPoetHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.attributeService = new AttributeService();
    }

    TypeSpec buildResourceNotFoundException() {
        // typeName ide u model za packagename posle
        FieldTypeNameWrapper responseTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", "exception");

        MethodSpec constructor = buildConstructor(null, "exception");

        List<Attribute> attributes = Arrays.asList(new Attribute(String.class, "resourceName"),
                new Attribute(String.class, "fieldName"),
                new Attribute(Object.class, "fieldValue"));

        List<FieldSpec> fieldSpecList = attributes.stream().map(attribute -> FieldSpec
                .builder(ClassName.bestGuess(attribute.getType().toString()), attribute.getFieldName())
                .addModifiers(Modifier.PRIVATE)
                .build()).collect(Collectors.toList());

        AttributeService attributeService = new AttributeService();
        List<MethodSpec> getterMethods = attributeService.buildGetters(attributes);
        return TypeSpec
                .classBuilder("ResourceNotFoundException")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec
                        .builder(ResponseStatus.class)
                        .addMember("value", "$L", HttpStatus.NOT_FOUND)
                        .build())
                .superclass(RuntimeException.class)
                .addFields(fieldSpecList)
                .addMethod(constructor)
                .addMethods(getterMethods)
                .build();
    }

    @Override
    public CodeBlock buildFindByIdCodeBlock(Entity entity) {
        FieldTypeNameWrapper responseTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", "exception");
        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");

        return CodeBlock.builder()
                .addStatement("Optional<$T> $L = $L.findById(id)", domainTypeNameWrapper.getTypeName(),
                        entity.getOptionalName(), daoTypeNameWrapper.getInstanceName())
                .beginControlFlow("if ($L.isEmpty())", entity.getOptionalName())
                .addStatement("throw new $T(\"$T $L\")", responseTypeNameWrapper.getTypeName(), responseTypeNameWrapper.getTypeName(), "not found!")
                .endControlFlow()
                .addStatement("$T $L = $L.get()", daoTypeNameWrapper.getTypeName(), entity.getInstanceName(), entity.getOptionalName())
                .build();
    }
    
    @Override
    public MethodSpec buildConstructor(Entity entity, String dependencyClass) {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder();
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<ParameterSpec> parameterSpecsList = new ArrayList<>();

        // For api class its serviceClass dependency, for serviceImpl class its dao dependency, if exception then skip
        if (dependencyClass.matches("daoClass|serviceClass")) {
            FieldTypeNameWrapper dependencyTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), dependencyClass);
            fieldSpecList.add(FieldSpec
                    .builder(dependencyTypeNameWrapper.getTypeName(), dependencyTypeNameWrapper.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            parameterSpecsList.add(ParameterSpec
                    .builder(dependencyTypeNameWrapper.getTypeName(), dependencyTypeNameWrapper.getInstanceName())
                    .build());
            methodBuilder.addAnnotation(Autowired.class);
        }

        if (dependencyClass.equals("exception")) {
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess("String"), "resourceName")
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess("String"), "fieldName")
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            fieldSpecList.add(FieldSpec
                    .builder(ClassName.bestGuess("Object"), "fieldValue")
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            methodBuilder.addStatement("super(String.format(\"%s not found with %s : '%s'\", resourceName, fieldName, fieldValue))");
        }

        if (dependencyClass.equals("daoClass")) {
            List<RelationAttribute> relationAttributes = attributeService.findRelationAttributes(entity);

            relationAttributes.forEach(attribute -> {

                FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(attribute.getClassSimpleName(), "serviceClass");

                fieldSpecList.add(FieldSpec
                        .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                        .addModifiers(Modifier.PRIVATE)
                        .build());
                parameterSpecsList.add(ParameterSpec
                        .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                        .build());
            });
        }

        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(cb -> builder.addStatement("this.$N = $N", cb.name, cb.name));


        return methodBuilder
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();
    }


}
