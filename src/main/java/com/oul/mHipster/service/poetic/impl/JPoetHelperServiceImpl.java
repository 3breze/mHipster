package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JPoetHelperServiceImpl implements JPoetHelperService {

    private EntityManagerService entityManagerService;

    public JPoetHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    public String injectImports(JavaFile javaFile, List<String> imports) {
        String rawSource = javaFile.toString();

        List<String> result = new ArrayList<>();
        for (String s : rawSource.split("\n", -1)) {
            result.add(s);
            if (s.startsWith("package ")) {
                result.add("");
                for (String i : imports) {
                    result.add("import " + i + ";");
                }
            }
        }
        return String.join("\n", result);
    }

    @Override
    public CodeBlock buildLombokBuilder(Entity entity) {

        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                "domainClass");
        FieldTypeNameWrapper requestTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                "requestClass");

        List<FieldSpec> fieldSpecList = entity.getAttributes().stream().map(attribute -> FieldSpec
                .builder(attribute.getType(), attribute.getFieldName())
                .addModifiers(Modifier.PRIVATE)
                .build()).collect(Collectors.toList());

        StringBuffer builderStingBuffer = new StringBuffer();
        fieldSpecList.forEach(field -> builderStingBuffer.append(".").append(field.name).append("(").append(requestTypeNameWrapper.getInstanceName()).append(".get")
                .append(ClassUtils.fieldGetter(field.name)).append(")\n"));
        return CodeBlock.builder()
                .addStatement("$T $L = $T.builder()$L.build()", domainTypeNameWrapper.getTypeName(),
                        domainTypeNameWrapper.getInstanceName(), domainTypeNameWrapper.getTypeName(), builderStingBuffer.toString())
                .build();
    }

    @Override
    public CodeBlock buildFindManyRelationCodeBlock(Entity entity, List<RelationAttribute> relationAttributes) {

        FieldTypeNameWrapper requestTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                "requestClass");
        FieldTypeNameWrapper listTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "List", "list");

        CodeBlock.Builder cbBuilder = CodeBlock.builder();

        relationAttributes.forEach(relationAttribute -> {
            FieldTypeNameWrapper serviceTypeNameWrapper = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "serviceClass");
            FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "domainClass");
            cbBuilder.addStatement("$T<$T> $L = $L.findByIds($L.get$LListIds())", listTypeNameWrapper.getTypeName(),
                    domainTypeNameWrapper.getTypeName(), domainTypeNameWrapper.getInstanceName() + "List",
                    serviceTypeNameWrapper.getInstanceName(), requestTypeNameWrapper.getInstanceName(),
                    ClassUtils.capitalizeField(domainTypeNameWrapper.getInstanceName()));
        });
        return cbBuilder.build();
    }

    @Override
    public CodeBlock buildFindOneRelationCodeBlock(Entity entity, List<RelationAttribute> relationAttributes) {

        FieldTypeNameWrapper rootTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        FieldTypeNameWrapper requestTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                "requestClass");

        CodeBlock.Builder cbBuilder = CodeBlock.builder();

        relationAttributes.forEach(relationAttribute -> {
            FieldTypeNameWrapper serviceTypeNameWrapper = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "serviceClass");
            FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "domainClass");
            cbBuilder.beginControlFlow("if (Optional.ofNullable($L.get$L()).isPresent())", requestTypeNameWrapper.getInstanceName(),
                    ClassUtils.capitalizeField(domainTypeNameWrapper.getInstanceName()) + "Id")
                    .addStatement("$T $L = $L.findOne($L.get$L())", domainTypeNameWrapper.getTypeName(),
                            domainTypeNameWrapper.getInstanceName(), serviceTypeNameWrapper.getInstanceName(),
                            requestTypeNameWrapper.getInstanceName(), ClassUtils.capitalizeField(domainTypeNameWrapper.getInstanceName()) + "Id")
                    .addStatement("$L.set$L($L)", rootTypeNameWrapper.getInstanceName(), ClassUtils.capitalizeField(domainTypeNameWrapper.getInstanceName()),
                            domainTypeNameWrapper.getInstanceName())
                    .endControlFlow();
        });
        return cbBuilder.build();
    }

    @Override
    public List<FieldSpec> buildRelationFieldSpecList(List<RelationAttribute> relationAttributes) {
        return relationAttributes.stream().map(attribute -> {
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(attribute.getTypeArgument(),
                    "serviceClass");
            return FieldSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public CodeBlock buildPageResponse(Entity entity) {
        FieldTypeNameWrapper collectorsTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "Collectors", "collectors");
        FieldTypeNameWrapper pageImplTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "PageImpl", "pageImpl");
        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");

        return CodeBlock.builder()
                .addStatement("new $T<>(page.stream().map($T::new).collect($T.toList()), pageable,page.getTotalElements())",
                        pageImplTypeNameWrapper.getTypeName(), domainTypeNameWrapper.getTypeName(), collectorsTypeNameWrapper.getTypeName())
                .build();
    }

    @Override
    public CodeBlock buildFindByIdCodeBlock(Entity entity) {

        FieldTypeNameWrapper responseTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", "exception");
        FieldTypeNameWrapper optionalTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "Optional", "optional");

        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");

        return CodeBlock.builder()
                .addStatement("$T<$T> $L = $L.findById(id)", optionalTypeNameWrapper.getTypeName(), domainTypeNameWrapper.getTypeName(),
                        entity.getOptionalName(), daoTypeNameWrapper.getInstanceName())
                .beginControlFlow("if ($L.isEmpty())", entity.getOptionalName())
                .addStatement("throw new $T(\"$T\", \"id\", id)", responseTypeNameWrapper.getTypeName(), responseTypeNameWrapper.getTypeName())
                .endControlFlow()
                .addStatement("$T $L = $L.get()", domainTypeNameWrapper.getTypeName(), entity.getInstanceName(), entity.getOptionalName())
                .build();
    }

    @Override
    public MethodSpec buildConstructor(List<FieldSpec> fieldSpecList, String dependencyClass) {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder();

        // For api class its serviceClass dependency, for serviceImpl class its dao dependency, if exception then skip
        if (dependencyClass.matches("daoClass|serviceClass")) {
            methodBuilder.addAnnotation(Autowired.class);
        } else if (dependencyClass.equals("exception")) {
            methodBuilder.addStatement("super(String.format(\"%s not found with %s : '%s'\", " +
                    "resourceName, fieldName, fieldValue))");
        }

        List<ParameterSpec> parameterSpecsList = fieldSpecList.stream()
                .map(fieldSpec -> ParameterSpec
                        .builder(fieldSpec.type, fieldSpec.name)
                        .build())
                .collect(Collectors.toList());

        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(field -> builder.addStatement("this.$N = $N", field.name, field.name));

        return methodBuilder
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();
    }

    @Override
    public CodeBlock buildSetterCallsCodeBlock(Entity entity) {

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.REQUEST_DTO.toString());

        List<FieldSpec> fieldSpecList = entity.getAttributes().stream().map(attribute -> FieldSpec
                .builder(attribute.getType(), attribute.getFieldName())
                .addModifiers(Modifier.PRIVATE)
                .build()).collect(Collectors.toList());

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        fieldSpecList.forEach(field -> codeBlockBuilder.addStatement("$L.set$N($L.get$N())", entity.getInstanceName(),
                ClassUtils.capitalizeField(field.name), classNamingInfo.getInstanceName(), ClassUtils.capitalizeField(field.name)));
        return codeBlockBuilder.build();
    }

    @Override
    public List<MethodSpec> buildGetters(List<FieldSpec> fieldSpecList) {
        return fieldSpecList.stream().map(fieldSpec -> {
            String fieldName = fieldSpec.name;
            String getterName = "get" + ClassUtils.capitalizeField(fieldName);
            return MethodSpec.methodBuilder(getterName)
                    .returns(fieldSpec.type)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $L", fieldName)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<MethodSpec> buildSetters(List<FieldSpec> fieldSpecList) {
        return fieldSpecList.stream().map(fieldSpec -> {
            String fieldName = fieldSpec.name;
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return MethodSpec.methodBuilder(setterName)
                    .addParameter(ParameterSpec
                            .builder(fieldSpec.type, fieldName)
                            .build())
                    .returns(TypeName.VOID)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this.$L = $L", fieldName, fieldName)
                    .build();
        }).collect(Collectors.toList());
    }
}
