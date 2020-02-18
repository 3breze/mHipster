package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.Method;
import com.oul.mHipster.layerconfig.Parameter;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.service.poetic.MethodBuilderService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodServiceImpl implements MethodBuilderService {

    private static final String REGEX = "\\$\\{(.*?)}";
    private static final String INSTANCE_SUFFIX = "Inst";
    private static final String INJECT_RELATION_FIND_BY_ID = "findByIdRelation";
    private static final String INJECT_FIND_BY_ID = "findByIdInject";
    private static final String INJECT_BUILDER = "builderInject";
    private static final String INJECT_SETTER_CALLS = "setterCalls";
    private static final String INJECT_PAGE_RES = "pageResInject";
    private static final String CLASS_SUFFIX = "Class";

    private EntityManagerService entityManagerService;
    private JPoetHelperService jPoetHelperService;
    private AttributeService attributeService;

    public MethodServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
    }

    @Override
    public CodeBlock processMethodBody(Entity entity, Method method) {

        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(method.getMethodBody());
        StringBuffer templateCode = new StringBuffer();
        CodeBlock.Builder cbBuilder = CodeBlock.builder();
        boolean flag = false;

        while (matcher.find()) {
            String injectKeyword = matcher.group(1);
            if (Arrays.asList(INJECT_FIND_BY_ID, INJECT_BUILDER, INJECT_SETTER_CALLS, INJECT_PAGE_RES)
                    .contains(injectKeyword)) {
                CodeBlock codeBlock = buildCodeBlock(injectKeyword, entity, method.getType());
                assert codeBlock != null;
                cbBuilder.add(codeBlock);
                matcher.appendReplacement(templateCode, "");
                continue;
            }
            if (injectKeyword.equals(INJECT_RELATION_FIND_BY_ID)) {
                if (templateCode.length() > 0) {
                    matcher.appendTail(templateCode);
                    String returnStatement = templateCode.toString().substring(0, templateCode.toString().indexOf("${"));
                    cbBuilder.addStatement(returnStatement.trim());
                    matcher.appendReplacement(templateCode, "");
                    templateCode.delete(0, templateCode.length());
                }
                Map<Boolean, List<RelationAttribute>> relationAttributes = attributeService.partitionParameterizedRelationAttributes(entity);
                CodeBlock findRelationCodeBlock = jPoetHelperService.buildFindRelationCodeBlock(entity, relationAttributes);
                cbBuilder.add(findRelationCodeBlock);
                continue;
            }
            if (injectKeyword.endsWith(INSTANCE_SUFFIX)) {
                flag = true;
                injectKeyword = injectKeyword.substring(0, injectKeyword.indexOf(INSTANCE_SUFFIX));
                injectKeyword += CLASS_SUFFIX;
            }
            if (injectKeyword.equals("responseClass")) {
                ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.RESPONSE_DTO.toString());
                matcher.appendReplacement(templateCode, classNamingInfo.getClassName());
            } else {
                TypeWrapper entityType = entityManagerService.getProperty(entity.getClassName(), injectKeyword);
                matcher.appendReplacement(templateCode, flag ? entityType.getInstanceName() : entity.getClassName());
            }
            flag = false;
        }
        //da ne bi dodavao prazan statement
        if (templateCode.length() > 0) {
            matcher.appendTail(templateCode);
            cbBuilder.addStatement(templateCode.toString());
        }
        return cbBuilder.build();
    }

    private CodeBlock buildCodeBlock(String injectKeyword, Entity entity, String methodType) {
        switch (injectKeyword) {
            case INJECT_FIND_BY_ID:
                return jPoetHelperService.buildFindByIdCodeBlock(entity, methodType);
            case INJECT_PAGE_RES:
                return jPoetHelperService.buildPageResponse(entity);
            case INJECT_SETTER_CALLS:
                return jPoetHelperService.buildSetterCallsCodeBlock(entity);
            case INJECT_BUILDER:
                return jPoetHelperService.buildLombokBuilder(entity);
        }
        throw new ConfigurationErrorException("Unknown code block inject keyword!");
    }

    @Override
    public List<ParameterSpec> getMethodParameters(Entity entity, Method method, String layer) {
        return method.getMethodSignature().getParameters().stream().map(parameter -> {
            TypeWrapper attributeType = attributeService.getTypeName(entity.getClassName(),
                    parameter.getType(), parameter.getName());
            ParameterSpec.Builder parameterBuilder = ParameterSpec
                    .builder(attributeType.getTypeName(), attributeType.getInstanceName());
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

        TypeWrapper updateValidationType = entityManagerService.getProperty("dependencies",
                "ValidationGroupUpdate", null);
        TypeWrapper saveValidationType = entityManagerService.getProperty("dependencies",
                "ValidationGroupSave", null);

        switch (method.getType()) {
            case "findAll":
                TypeWrapper domainType = entityManagerService.getProperty(entity.getClassName(), "domainClass");
                TypeWrapper daoType = entityManagerService.getProperty(entity.getClassName(), "daoClass");
                if (parameter.getType().equals("Predicate")) {
                    parameterBuilder.addAnnotation(AnnotationSpec
                            .builder(QuerydslPredicate.class)
                            .addMember("root", "$T.$L", domainType.getTypeName(), "class")
                            .addMember("bindings", "$T.$L", daoType.getTypeName(), "class")
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
                        .addMember("value", "$T.$L", saveValidationType.getTypeName(), "class")
                        .build());
                parameterBuilder.addAnnotation(RequestBody.class);
                break;
            case "update":
                parameterBuilder.addAnnotation(AnnotationSpec
                        .builder(Validated.class)
                        .addMember("value", "$T.$L", updateValidationType.getTypeName(), "class")
                        .build());
                parameterBuilder.addAnnotation(RequestBody.class);
                break;
        }
        return parameterBuilder;
    }


}
