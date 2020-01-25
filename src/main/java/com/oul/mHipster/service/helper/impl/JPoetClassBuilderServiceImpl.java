package com.oul.mHipster.service.helper.impl;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.helper.JPoetClassBuilderService;
import com.squareup.javapoet.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.lang.model.element.Modifier;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPoetClassBuilderServiceImpl extends JPoetHelperServiceImpl implements JPoetClassBuilderService {

    private EntityManagerService entityManagerService;

    public JPoetClassBuilderServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    @Override
    public TypeSpec buildResourceNotFoundException() {

        // typeName ide u model za packagename posle
        FieldTypeNameWrapper responseTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", "exception");

        List<Attribute> attributes = Arrays.asList(new Attribute(String.class, "resourceName"),
                new Attribute(String.class, "fieldName"),
                new Attribute(Object.class, "fieldValue"));

        List<FieldSpec> fieldSpecList = attributes.stream().map(attribute -> FieldSpec
                .builder(ClassName.bestGuess(attribute.getType().toString()), attribute.getFieldName())
                .addModifiers(Modifier.PRIVATE)
                .build()).collect(Collectors.toList());
        MethodSpec constructor = buildConstructor(null, fieldSpecList, "exception");

        List<MethodSpec> getterMethods = buildGetters(attributes);
        List<MethodSpec> setterMethods = buildSetters(attributes);

        return TypeSpec
                .classBuilder("ResourceNotFoundException")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec
                        .builder(ResponseStatus.class)
                        .addMember("value", "$T.$L", HttpStatus.class,
                                HttpStatus.NOT_FOUND.name())
                        .build())
                .superclass(RuntimeException.class)
                .addFields(fieldSpecList)
                .addMethod(constructor)
                .addMethods(getterMethods)
                .addMethods(setterMethods)
                .build();
    }

    @Override
    public TypeSpec buildValidationGroup() {
        return TypeSpec
                .classBuilder("ValidationGroup")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addType(TypeSpec.interfaceBuilder("Save").addSuperinterface(Default.class).build())
                .addType(TypeSpec.interfaceBuilder("Update").addSuperinterface(Default.class).build())
                .build();
    }
}
