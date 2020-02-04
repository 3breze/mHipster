package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.service.poetic.JPoetClassService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.lang.model.element.Modifier;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPoetClassServiceImpl extends JPoetHelperServiceImpl implements JPoetClassService {

    private EntityManagerService entityManagerService;

    public JPoetClassServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    @Override
    public TypeSpec buildResourceNotFoundException() {

        // typeName ide u model za packagename posle
        FieldTypeNameWrapper responseTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", null);

        List<Attribute> attributes = Arrays.asList(new Attribute(String.class, "resourceName"),
                new Attribute(String.class, "fieldName"),
                new Attribute(Object.class, "fieldValue"));

        List<FieldSpec> fieldSpecList = attributes.stream().map(attribute -> FieldSpec
                .builder(attribute.getType(), attribute.getFieldName())
                .addModifiers(Modifier.PRIVATE)
                .build()).collect(Collectors.toList());
        MethodSpec constructor = buildConstructor(fieldSpecList, "exception");

        List<MethodSpec> getterMethods = buildGetters(fieldSpecList);
        List<MethodSpec> setterMethods = buildSetters(fieldSpecList);

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

        FieldTypeNameWrapper validationGroupTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ValidationGroup", "exception");

        return TypeSpec
                .classBuilder("ValidationGroup")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addType(TypeSpec.interfaceBuilder("Save").addSuperinterface(Default.class).addModifiers(Modifier.PUBLIC).build())
                .addType(TypeSpec.interfaceBuilder("Update").addSuperinterface(Default.class).addModifiers(Modifier.PUBLIC).build())
                .build();
    }
}
