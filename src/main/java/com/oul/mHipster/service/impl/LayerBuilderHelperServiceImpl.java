package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.LayerBuilderHelperService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LayerBuilderHelperServiceImpl implements LayerBuilderHelperService {

    private EntityManagerService entityManagerService;

    public LayerBuilderHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
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
            if (str.endsWith(suffix)) {
                flag = true;
                str = str.substring(0, str.indexOf(suffix));
            }
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(), str);
            matcher.appendReplacement(sb, flag ? typeNameWrapper.getInstanceName() : typeNameWrapper.getTypeName().toString());
        }
        matcher.appendTail(sb);
        String codeBlock = sb.toString();

        String codeBlock1 = codeBlock.replaceAll("&lt;", "<");
        String codeBlock2 = codeBlock1.replaceAll("&gt;", ">");

        return CodeBlock.builder().addStatement(codeBlock2).build();
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
