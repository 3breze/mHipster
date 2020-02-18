package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.Layer;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.service.poetic.MethodBuilderService;
import com.oul.mHipster.service.poetic.impl.AttributeService;
import com.oul.mHipster.service.poetic.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.poetic.impl.MethodServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;
    private AttributeService attributeService;
    private MethodBuilderService methodBuilderService;

    public GenerateServiceImplClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
        this.methodBuilderService = new MethodServiceImpl();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        List<FieldSpec> fieldSpecList = attributeService.buildRelationFieldSpecList(entity);

        TypeWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");
        fieldSpecList.add(FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build());

        MethodSpec constructor = jPoetHelperService.buildConstructor(fieldSpecList, "daoClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("SERVICE_IMPL"))
                .findFirst();
        if (serviceImplLayerOptional.isEmpty()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());

            CodeBlock methodBody = methodBuilderService.processMethodBody(entity, method);

            List<ParameterSpec> parameters = methodBuilderService.getMethodParameters(entity, method, LayerName.SERVICE_IMPL.name());

            TypeWrapper returnTypeName = attributeService.getTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns(), null);

            return methodBuilder
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .addCode(methodBody)
                    .returns(returnTypeName.getTypeName())
                    .build();
        }).collect(Collectors.toList());

        TypeWrapper serviceTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "serviceClass");

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.SERVICE_IMPL.toString());

        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addSuperinterface(serviceTypeNameWrapper.getTypeName())
                .addFields(fieldSpecList)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
