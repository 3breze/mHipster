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
import java.util.stream.Collectors;

public class LayerBuilderHelperServiceImpl implements LayerBuilderHelperService {

    private EntityManagerService entityManagerService;

    public LayerBuilderHelperServiceImpl() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    @Override
    public CodeBlock processMethodBody(String methodBody) {
        return null;
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
