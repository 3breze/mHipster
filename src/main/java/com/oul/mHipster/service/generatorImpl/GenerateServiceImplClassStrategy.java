package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.JPoetHelperService;
import com.oul.mHipster.service.impl.AttributeService;
import com.oul.mHipster.service.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService JPoetHelperService;
    private LayersConfig layersConfig;
    private EntityManagerService entityManagerService;
    private AttributeService attributeService;

    public GenerateServiceImplClassStrategy() {
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.layersConfig = Util.getValue();
        this.JPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
}

    @Override
    public TypeSpec generate(Entity entity) {
        CodeBlock throwExceptionCodeBlock = JPoetHelperService.buildFindByIdCodeBlock(entity);

        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");
        FieldSpec daoField = FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build();

        // dependencyClass -> needs DAO dependency
        MethodSpec constructor = JPoetHelperService.buildConstructor(entity, "daoClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("SERVICE_IMPL")).findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = new ArrayList<>();
        serviceImplLayerOptional.get().getMethods().forEach(method -> {

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());

            List<ParameterSpec> parameters = new ArrayList<>();
            method.getMethodSignature().getParameters().forEach(parameter -> {
                FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(entity.getClassName(),
                        parameter.getType(), parameter.getName());

                parameters.add(
                        ParameterSpec
                                .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                                .build());
            });

            TypeName returnTypeName = attributeService.getReturnTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns());

            methods.add(methodBuilder
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .returns(returnTypeName)
                    .build());
        });

        FieldTypeNameWrapper serviceTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "serviceClass");

        return TypeSpec
                .classBuilder(entity.getLayers().get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addSuperinterface(serviceTypeNameWrapper.getTypeName())
                .addField(daoField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
