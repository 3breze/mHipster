package com.oul.mHipster.service.helper.impl;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.layersConfig.Parameter;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.helper.JPoetHelperService;
import com.oul.mHipster.service.helper.MethodBuilderService;
import com.squareup.javapoet.*;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodBuilderServiceImpl implements MethodBuilderService {

    private static final String REGEX = "\\$\\{(.*?)}";
    private static final String INSTANCE_SUFFIX = "Inst";
    private static final String INJECT_RELATION_FIND_BY_ID = "findByIdRelation";
    private static final String INJECT_FIND_BY_ID = "findByIdInject";
    private static final String INJECT_BUILDER = "builderInject";
    private static final String INJECT_SETTER_CALLS = "setterCalls";
    private static final String INJECT_OPTIONAL = "optionalInst";
    private static final String CLASS_SUFFIX = "Class";

    private EntityManagerService entityManagerService;
    private JPoetHelperService jPoetHelperService;
    private AttributeBuilderService attributeBuilderService;

    public MethodBuilderServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeBuilderService = new AttributeBuilderService();
    }

    @Override
    public CodeBlock processMethodBody(Entity entity, String methodBody) {

        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(methodBody);
        StringBuffer templateCode = new StringBuffer();
        CodeBlock.Builder cbBuilder = CodeBlock.builder();
        boolean flag = false;

        while (matcher.find()) {
            String injectKeyword = matcher.group(1);

            if (injectKeyword.equals(INJECT_RELATION_FIND_BY_ID)) {
                List<RelationAttribute> relationAttributes = attributeBuilderService.findRelationAttributes(entity);
                if (!relationAttributes.isEmpty()) {
                    CodeBlock relationFindByIdCodeBlock = jPoetHelperService.buildRelationFindByIdCodeBlock(entity, relationAttributes);
                    cbBuilder.add(relationFindByIdCodeBlock);
                }
                matcher.appendReplacement(templateCode, "");
                continue;
            }
            if (injectKeyword.equals(INJECT_FIND_BY_ID)) {
                CodeBlock findByIdCodeBlock = jPoetHelperService.buildFindByIdCodeBlock(entity);
                cbBuilder.add(findByIdCodeBlock);
                matcher.appendReplacement(templateCode, "");
                continue;
            }
            if (injectKeyword.equals(INJECT_SETTER_CALLS)) {
                CodeBlock setterCallsCodeBlock = jPoetHelperService.buildSetterCallsCodeBlock(entity);
                cbBuilder.add(setterCallsCodeBlock);
                matcher.appendReplacement(templateCode, "");
                continue;
            }
            if (injectKeyword.equals(INJECT_BUILDER)) {
                CodeBlock lombokBuilderCodeBlock = jPoetHelperService.buildLombokBuilder(entity);
                cbBuilder.add(lombokBuilderCodeBlock);
                matcher.appendReplacement(templateCode, "");
                continue;
            }
            if (injectKeyword.equals(INJECT_OPTIONAL)) {
                matcher.appendReplacement(templateCode, entity.getOptionalName());
                continue;
            }
            if (injectKeyword.endsWith(INSTANCE_SUFFIX)) {
                flag = true;
                injectKeyword = injectKeyword.substring(0, injectKeyword.indexOf(INSTANCE_SUFFIX));
                injectKeyword += CLASS_SUFFIX;
            }

            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(), injectKeyword);
            matcher.appendReplacement(templateCode, flag ? typeNameWrapper.getInstanceName() : entity.getClassName());
            flag = false;
        }

        matcher.appendTail(templateCode);
        cbBuilder.addStatement(templateCode.toString());
        return cbBuilder.build();
    }

    @Override
    public List<ParameterSpec> getMethodParameters(Entity entity, Method method, String layer) {
        return method.getMethodSignature().getParameters().stream().map(parameter -> {
            FieldTypeNameWrapper typeNameWrapper = attributeBuilderService.getReturnTypeName(entity.getClassName(),
                    parameter.getType(), parameter.getName());
            ParameterSpec.Builder parameterBuilder = ParameterSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName());
            if (layer.equals(LayerName.API.name())) {
                parameterBuilder = processMethodSignature(entity, method, parameter, parameterBuilder);
            }
            return parameterBuilder.build();
        }).collect(Collectors.toList());
    }

    public String getRequestMethod(String method) {
        String requestMethodName = RequestMethod.GET.name();

        switch (method) {
            case "save":
                requestMethodName = RequestMethod.POST.name();
                break;
            case "update":
                requestMethodName = RequestMethod.PUT.name();
                break;
        }
        return requestMethodName;
    }

    @Override
    public ParameterSpec.Builder processMethodSignature(Entity entity, Method method, Parameter parameter,
                                                        ParameterSpec.Builder parameterBuilder) {

        TypeName saveValidationGroupTypeName = ClassName.get("com.whatever.whatever.ValidationGroup",
                "Save");
        TypeName updateValidationGroupTypeName = ClassName.get("com.whatever.whatever.ValidationGroup",
                "Update");

        switch (method.getType()) {
            case "findAll":
                FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");
                FieldTypeNameWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");
                if (parameter.getType().equals("Predicate")) {
                    parameterBuilder.addAnnotation(AnnotationSpec
                            .builder(QuerydslPredicate.class)
                            .addMember("root", "$T.$L", domainTypeNameWrapper.getTypeName(), "class")
                            .addMember("bindings", "$T.$L", daoTypeNameWrapper.getTypeName(), "class")
                            .build());
                    break;
                }
                parameterBuilder.addAnnotation(AnnotationSpec
                        .builder(PageableDefault.class)
                        .addMember("value", "$L", 20)
                        .addMember("page", "$L", 0)
                        .build());
                break;
            case "findById":
            case "delete":
                parameterBuilder.addAnnotation(AnnotationSpec
                        .builder(RequestParam.class)
                        .addMember("name", "$S", "id")
                        .build());
                break;
            case "save":

                parameterBuilder.addAnnotation(AnnotationSpec
                        .builder(Validated.class)
                        .addMember("value", "$T.$L", saveValidationGroupTypeName, "class")
                        .build());
                parameterBuilder.addAnnotation(RequestBody.class);
                break;
            case "update":
                parameterBuilder.addAnnotation(AnnotationSpec
                        .builder(Validated.class)
                        .addMember("value", "$T.$L", updateValidationGroupTypeName, "class")
                        .build());
                parameterBuilder.addAnnotation(RequestBody.class);
                break;
        }
        return parameterBuilder;
    }


}
