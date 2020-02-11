package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.util.ClassUtils;
import com.oul.mHipster.util.ReflectionUtil;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JPoetHelperServiceImpl implements JPoetHelperService {

    private EntityManagerService entityManagerService;

    public JPoetHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    public CodeBlock buildFindRelationCodeBlock(Entity entity, Map<Boolean, List<RelationAttribute>> relationAttributes) {
        CodeBlock.Builder cbBuilder = CodeBlock.builder();

        if (!relationAttributes.get(true).isEmpty()) {
            CodeBlock.Builder findManyRelationCodeBlock = buildFindManyRelationCodeBlock(entity, relationAttributes.get(true));
            cbBuilder.add(findManyRelationCodeBlock.build());
        }

        if (!relationAttributes.get(false).isEmpty()) {
            CodeBlock.Builder findOneRelationCodeBlock = buildFindOneRelationCodeBlock(entity, relationAttributes.get(false));
            cbBuilder.add(findOneRelationCodeBlock.build());
        }
        return cbBuilder.build();
    }

    private CodeBlock.Builder buildFindManyRelationCodeBlock(Entity entity, List<RelationAttribute> relationAttributes) {

        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(),
                "domainClass");
        TypeWrapper requestType = entityManagerService.getProperty(entity.getClassName(),
                "requestClass");
        TypeWrapper listType = entityManagerService.getProperty("dependencies",
                "List", "list");

        CodeBlock.Builder cbBuilder = CodeBlock.builder();

        String entityName = entity.getClassName();
        IntStream.range(0, relationAttributes.size()).mapToObj(i -> {
            String relationName = relationAttributes.get(i).getTypeArgument();
            Map<String, Object[]> result = entityManagerService.getStatementArgs("buildFindManyRelationCodeBlock",
                    i, List.of("List", relationName, relationName, relationName, entityName, relationName));
        })

        relationAttributes.forEach(relationAttribute -> {


            TypeWrapper relationServiceType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "serviceClass");
            TypeWrapper relationDomainType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "domainClass");
            cbBuilder.addStatement("$T<$T> $LList = $L.findByIds($L.get$LListIds())", listType.getTypeName(),
                    relationDomainType.getTypeName(), relationDomainType.getInstanceName(),
                    relationServiceType.getInstanceName(), requestType.getInstanceName(),
                    ClassUtils.capitalizeField(relationDomainType.getInstanceName()));
            cbBuilder.addStatement("$L.set$LList($LList)", domainType.getInstanceName(), ClassUtils.capitalizeField(relationDomainType.getInstanceName()),
                    relationDomainType.getInstanceName());
        });
        return cbBuilder;
    }


    private CodeBlock.Builder buildFindOneRelationCodeBlock(Entity entity, List<RelationAttribute> relationAttributes) {

        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        TypeWrapper requestType = entityManagerService.getProperty(entity.getClassName(), "requestClass");

        CodeBlock.Builder cbBuilder = CodeBlock.builder();

        relationAttributes.forEach(relationAttribute -> {
            TypeWrapper relationServiceType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "serviceClass");
            TypeWrapper relationDomainType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "domainClass");
            cbBuilder.beginControlFlow("if (Optional.ofNullable($L.get$LId()).isPresent())", requestType.getInstanceName(),
                    ClassUtils.capitalizeField(relationDomainType.getInstanceName()))
                    .addStatement("$T $L = $L.findOne($L.get$LId())", relationDomainType.getTypeName(),
                            relationDomainType.getInstanceName(), relationServiceType.getInstanceName(),
                            requestType.getInstanceName(), ClassUtils.capitalizeField(relationDomainType.getInstanceName()))
                    .addStatement("$L.set$L($L)", domainType.getInstanceName(), ClassUtils.capitalizeField(relationDomainType.getInstanceName()),
                            relationDomainType.getInstanceName())
                    .endControlFlow();
        });
        return cbBuilder;
    }

    @Override
    public List<FieldSpec> buildRelationFieldSpecList(List<RelationAttribute> relationAttributes) {
        return relationAttributes.stream().map(attribute -> {
            TypeWrapper serviceType = entityManagerService.getProperty(attribute.getTypeArgument(),
                    "serviceClass");
            return FieldSpec
                    .builder(serviceType.getTypeName(), serviceType.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public CodeBlock buildLombokBuilder(Entity entity) {

        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(),
                "domainClass");
        TypeWrapper requestType = entityManagerService.getProperty(entity.getClassName(),
                "requestClass");
        Predicate<Attribute> predicate = RelationAttribute.class::isInstance;
        List<FieldSpec> fieldSpecList = entity.getAttributes().stream()
                .filter(predicate.negate())
                .map(attribute -> FieldSpec
                        .builder(attribute.getType(), attribute.getFieldName())
                        .addModifiers(Modifier.PRIVATE)
                        .build()).collect(Collectors.toList());

        StringBuffer builderStingBuffer = new StringBuffer();
        fieldSpecList.forEach(field -> builderStingBuffer.append(".").append(field.name).append("(")
                .append(requestType.getInstanceName()).append(".get")
                .append(ClassUtils.fieldGetter(field.name)).append(")\n"));
        return CodeBlock.builder()
                .addStatement("$T $L = $T.builder()$L.build()", domainType.getTypeName(),
                        domainType.getInstanceName(), domainType.getTypeName(), builderStingBuffer.toString())
                .build();
    }

    @Override
    public CodeBlock buildPageResponse(Entity entity) {
        TypeWrapper collectorsType = entityManagerService.getProperty("dependencies",
                "Collectors", "collectors");
        TypeWrapper pageImplType = entityManagerService.getProperty("dependencies",
                "PageImpl", "pageImpl");
        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        TypeWrapper responseType = entityManagerService.getProperty(entity.getClassName(), "responseClass");
        TypeWrapper daoType = entityManagerService.getProperty(entity.getClassName(), "daoClass");

        return CodeBlock.builder()
                .addStatement("Page<$T> page = $L.findAll(predicate, pageable)", domainType.getTypeName(), daoType.getInstanceName())
                .addStatement("return new $T<>(page.stream().map($T::new).collect($T.toList()), pageable, page.getTotalElements())",
                        pageImplType.getTypeName(), responseType.getTypeName(), collectorsType.getTypeName())
                .build();
    }

    @Override
    public CodeBlock buildFindByIdCodeBlock(Entity entity, String methodType) {

        TypeWrapper exceptionType = entityManagerService.getProperty("dependencies",
                "ResourceNotFoundException", "exception");
        TypeWrapper optionalType = entityManagerService.getProperty("dependencies",
                "Optional", "optional");

        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(), "domainClass");
        TypeWrapper requestType = entityManagerService.getProperty(entity.getClassName(), "requestClass");
        TypeWrapper daoType = entityManagerService.getProperty(entity.getClassName(), "daoClass");

        CodeBlock.Builder cbBuilder = CodeBlock.builder();
        if (methodType.equals("update")) {
            cbBuilder.addStatement("$T<$T> $L = $L.findById($L.getId())", optionalType.getTypeName(), domainType.getTypeName(),
                    entity.getOptionalName(), daoType.getInstanceName(), requestType.getInstanceName());
        } else {
            cbBuilder.addStatement("$T<$T> $L = $L.findById(id)", optionalType.getTypeName(), domainType.getTypeName(),
                    entity.getOptionalName(), daoType.getInstanceName());
        }
        cbBuilder.beginControlFlow("if ($L.isEmpty())", entity.getOptionalName());
        if (methodType.equals("update")) {
            cbBuilder.addStatement("throw new $T(\"$T\", \"id\", $L.getId())", exceptionType.getTypeName(),
                    exceptionType.getTypeName(), requestType.getInstanceName());
        } else {
            cbBuilder.addStatement("throw new $T(\"$T\", \"id\", id)", exceptionType.getTypeName(), exceptionType.getTypeName());
        }

        cbBuilder.endControlFlow()
                .addStatement("$T $L = $L.get()", domainType.getTypeName(), entity.getInstanceName(), entity.getOptionalName());
        return cbBuilder.build();
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

        List<ParameterSpec> parameterSpecList = fieldSpecList.stream()
                .map(fieldSpec -> ParameterSpec
                        .builder(fieldSpec.type, fieldSpec.name)
                        .build())
                .collect(Collectors.toList());

        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(field -> builder.addStatement("this.$N = $N", field.name, field.name));

        return methodBuilder
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecList)
                .addCode(builder.build())
                .build();
    }

    @Override
    public MethodSpec buildResponseConstructor(Entity entity, List<FieldSpec> attributeList) {

        CodeBlock.Builder builder = CodeBlock.builder();
        attributeList.forEach(field -> builder.addStatement("this.$L = $L.get$L()", field.name,
                entity.getInstanceName(), ClassUtils.capitalizeField(field.name)));

        Map<Boolean, List<RelationAttribute>> parameterizedPartition = entity.getAttributes().stream()
                .filter(RelationAttribute.class::isInstance)
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.partitioningBy(attribute -> ReflectionUtil.isParameterizedType(attribute.getType())));

        parameterizedPartition.get(true).forEach(relationAttribute -> {
            TypeWrapper responseType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "responseClass", relationAttribute.getFieldName());
            TypeWrapper collectorsType = entityManagerService.getProperty("dependencies",
                    "Collectors", "collectors");

            builder.addStatement("this.$L = $L.get$L().stream().map($T::new).collect($T.toList())",
                    relationAttribute.getFieldName(), entity.getInstanceName(),
                    ClassUtils.capitalizeField(relationAttribute.getFieldName()),
                    responseType.getTypeName(), collectorsType.getTypeName());
        });

        parameterizedPartition.get(false).forEach(relationAttribute -> {
            TypeWrapper responseType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                    "responseClass", relationAttribute.getFieldName());

            builder.addStatement("this.$L = $T.isNull($L.get$L()) ? null : new $T($L.get$L())",
                    relationAttribute.getFieldName(), ClassName.get(Objects.class), entity.getInstanceName(),
                    ClassUtils.capitalizeField(relationAttribute.getFieldName()),
                    responseType.getTypeName(), entity.getInstanceName(),
                    ClassUtils.capitalizeField(relationAttribute.getFieldName()));
        });

        TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(),
                "domainClass");
        ParameterSpec parameterSpec = ParameterSpec.builder(domainType.getTypeName(), domainType.getInstanceName()).build();

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec)
                .addCode(builder.build())
                .build();
    }

    @Override
    public CodeBlock buildSetterCallsCodeBlock(Entity entity) {

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.REQUEST_DTO.toString());

        Predicate<Attribute> predicate = RelationAttribute.class::isInstance;
        List<FieldSpec> fieldSpecList = entity.getAttributes().stream()
                .filter(predicate.negate())
                .map(attribute -> FieldSpec
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
