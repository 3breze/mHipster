package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.JPoetHelperService;
import com.oul.mHipster.service.LayerBuilderHelperService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LayerBuilderHelperServiceImpl implements LayerBuilderHelperService {

    private EntityManagerService entityManagerService;
    private JPoetHelperService jPoetHelperService;

    public LayerBuilderHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
    }

    @Override
    public CodeBlock processMethodBody(Entity entity, String methodBody) {

        String regex = "\\$\\{(.*?)}";
        String suffix = "Inst";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(methodBody);
        StringBuffer sb = new StringBuffer();
        boolean flag = false;
        while (matcher.find()) {
            String str = matcher.group(1);
            if (str.equals("findByIdRelation")) {
                matcher.appendReplacement(sb, "[...code to be added in...]");
                continue;
            }
            if (str.equals("findByIdInject")) {
                CodeBlock findByIdCodeBlock = jPoetHelperService.buildFindByIdCodeBlock(entity);
                matcher.appendReplacement(sb, findByIdCodeBlock.toString());
                continue;
            }
            if (str.equals("builderInject")) {
                CodeBlock lombokBuilderCodeBlock = jPoetHelperService.buildLombokBuilder(entity);
                matcher.appendReplacement(sb, lombokBuilderCodeBlock.toString());
                continue;
            }
            if (str.equals("optionalInst")) {
                matcher.appendReplacement(sb, entity.getOptionalName());
                continue;
            }
            if (str.endsWith(suffix)) {
                flag = true;
                str = str.substring(0, str.indexOf(suffix));
                str += "Class";
            }
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(), str);
            matcher.appendReplacement(sb, flag ? typeNameWrapper.getInstanceName() : entity.getClassName());
            flag = false;
        }
        matcher.appendTail(sb);

        System.out.println("----");
        return CodeBlock.builder().addStatement(sb.toString()).build();
    }

    @Override
    public List<ParameterSpec> resolveParameters(Entity entity, Method method) {
        return method.getMethodSignature().getParameters().stream().map(parameter -> {
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                    parameter.getType(), parameter.getName());
            return ParameterSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .build();
        }).collect(Collectors.toList());
    }
}
