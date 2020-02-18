package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.layerconfig.wrapper.CodeBlockStatement;
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
import org.springframework.data.querydsl.binding.QuerydslBindings;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JPoetHelperServiceImpl implements JPoetHelperService {

    private EntityManagerService entityManagerService;
    private AttributeService attributeService;

    public JPoetHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.attributeService = new AttributeService();
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

        CodeBlock.Builder cbBuilder = CodeBlock.builder();
        String entityName = entity.getClassName();
        relationAttributes.forEach(relationAttribute -> {

            String relationName = relationAttribute.getTypeArgument();
            CodeBlockStatement statement0 = entityManagerService.computeStatement("buildFindManyRelationCodeBlock",
                    0, Map.of("type", "dependencies", "relation", relationName, "entity", entityName));
            CodeBlockStatement statement1 = entityManagerService.computeStatement("buildFindManyRelationCodeBlock",
                    1, Map.of("entity", entityName, "relation", relationName));
            cbBuilder.addStatement(statement0.getStatementBody(), statement0.getResponseArgs());
            cbBuilder.addStatement(statement1.getStatementBody(), statement1.getResponseArgs());
        });

        return cbBuilder;
    }

    private CodeBlock.Builder buildFindOneRelationCodeBlock(Entity entity, List<RelationAttribute> relationAttributes) {

        CodeBlock.Builder cbBuilder = CodeBlock.builder();
        String entityName = entity.getClassName();
        relationAttributes.forEach(relationAttribute -> {
            String relationName = relationAttribute.getTypeArgument();

            CodeBlockStatement statement0 = entityManagerService.computeStatement("buildFindOneRelationCodeBlock",
                    0, Map.of("optional", "dependencies", "relation", relationName, "entity", entityName));
            CodeBlockStatement statement1 = entityManagerService.computeStatement("buildFindOneRelationCodeBlock",
                    1, Map.of("relation", relationName, "entity", entityName));
            CodeBlockStatement statement2 = entityManagerService.computeStatement("buildFindOneRelationCodeBlock",
                    2, Map.of("relation", relationName, "entity", entityName));

            cbBuilder.beginControlFlow(statement0.getStatementBody(), statement0.getResponseArgs())
                    .addStatement(statement1.getStatementBody(), statement1.getResponseArgs())
                    .addStatement(statement2.getStatementBody(), statement2.getResponseArgs())
                    .endControlFlow();
        });
        return cbBuilder;
    }

    @Override
    public CodeBlock buildLombokBuilder(Entity entity) {

        List<FieldSpec> fieldSpecList = attributeService.getNonRelationFieldSpecList(entity);
        String builderInject = attributeService.getBuilderFields(entity, fieldSpecList);

        CodeBlockStatement statement0 = entityManagerService.computeStatement("buildLombokBuilder",
                0, Map.of("stringbuilder", builderInject, "entity", entity.getClassName()));

        return CodeBlock.builder()
                .addStatement(statement0.getStatementBody(), statement0.getResponseArgs())
                .build();
    }

    @Override
    public CodeBlock buildPageResponse(Entity entity) {

        String entityName = entity.getClassName();
        CodeBlockStatement statement0 = entityManagerService.computeStatement("buildPageResponse",
                0, Map.of("entity", entityName));
        CodeBlockStatement statement1 = entityManagerService.computeStatement("buildPageResponse",
                1, Map.of("pageImpl", "dependencies", "collectors", "dependencies", "entity", entityName));

        return CodeBlock.builder()
                .addStatement(statement0.getStatementBody(), statement0.getResponseArgs())
                .addStatement(statement1.getStatementBody(), statement1.getResponseArgs())
                .build();
    }

    @Override
    public CodeBlock buildFindByIdCodeBlock(Entity entity) {

        String entityName = entity.getClassName();
        CodeBlockStatement statement0 = entityManagerService.computeStatement("buildFindByIdCodeBlock",
                0, Map.of("entity", entityName, "exception", "dependencies"));

        return CodeBlock.builder().addStatement(statement0.getStatementBody(), statement0.getResponseArgs()).build();
    }

    @Override
    public MethodSpec buildCustomizeMethod(Entity entity) {

        TypeWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");

        List<ParameterSpec> parameterSpecs = Arrays.asList(ParameterSpec.builder(QuerydslBindings.class, "bindings").build(),
                ParameterSpec
                        .builder(ClassName.bestGuess("Q" + ClassUtils.capitalizeField(domainTypeNameWrapper.getInstanceName())), "root")
                        .build());
        return MethodSpec.methodBuilder("customize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.DEFAULT)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameters(parameterSpecs)
                .addStatement("bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);")
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

            CodeBlockStatement statement0 = entityManagerService.computeStatement("buildResponseConstructor",
                    0, Map.of("relation", relationAttribute.getTypeArgument(), "entityName", entity.getInstanceName(),
                            "collectors", "dependencies", "relationName", relationAttribute.getFieldName()));
            builder.addStatement(statement0.getStatementBody(), statement0.getResponseArgs());
        });

        parameterizedPartition.get(false).forEach(relationAttribute -> {

            CodeBlockStatement statement1 = entityManagerService.computeStatement("buildResponseConstructor",
                    1, Map.of("relation", relationAttribute.getTypeArgument(), "entityName", entity.getInstanceName(),
                            "relationName", relationAttribute.getFieldName(), "objects", "dependencies"));
            builder.addStatement(statement1.getStatementBody(), statement1.getResponseArgs());
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

        List<FieldSpec> fieldSpecList = attributeService.getNonRelationFieldSpecList(entity);

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
