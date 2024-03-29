package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.Layer;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.poetic.MethodBuilderService;
import com.oul.mHipster.service.poetic.impl.AttributeService;
import com.oul.mHipster.service.poetic.impl.MethodServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateServiceClassStrategy implements GenerateLayerStrategy {

    private AttributeService attributeService;
    private MethodBuilderService methodBuilderService;

    public GenerateServiceClassStrategy() {
        this.attributeService = new AttributeService();
        this.methodBuilderService = new MethodServiceImpl();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("SERVICE_IMPL"))
                .findFirst();
        if (serviceImplLayerOptional.isEmpty()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());


            List<ParameterSpec> parameters = methodBuilderService.getMethodParameters(entity, method, LayerName.SERVICE.name());

            TypeWrapper returnTypeName = attributeService.getTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns(), null);

            return methodBuilder
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameters(parameters)
                    .returns(returnTypeName.getTypeName())
                    .build();
        }).collect(Collectors.toList());

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.SERVICE.toString());

        TypeSpec typeSpec = TypeSpec
                .interfaceBuilder(classNamingInfo.getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
