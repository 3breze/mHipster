package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.JPoetHelperService;
import com.oul.mHipster.service.MethodBuilderHelperService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodBuilderHelperServiceImpl implements MethodBuilderHelperService {

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
    private AttributeService attributeService;

    public MethodBuilderHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
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
                List<RelationAttribute> relationAttributes = attributeService.findRelationAttributes(entity);
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
    public List<ParameterSpec> resolveMethodParameters(Entity entity, Method method) {
        return method.getMethodSignature().getParameters().stream().map(parameter -> {
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                    parameter.getType(), parameter.getName());
            return ParameterSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .build();
        }).collect(Collectors.toList());
    }
}
