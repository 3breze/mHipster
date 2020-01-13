package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.PoetHelperService;
import com.oul.mHipster.service.impl.EntityManagerFactoryImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private PoetHelperService poetHelperService;
    private LayersConfig layersConfig;
    private EntityManagerFactory entityManagerFactory;

    public GenerateServiceImplClassStrategy() {
        this.poetHelperService = new PoetHelperService();
        this.entityManagerFactory = EntityManagerFactoryImpl.getInstance();
        this.layersConfig = Util.getValue();
    }

    @Override
    public TypeSpec generate(Entity entity) {
//        Map<String, ClassNamingInfo> layerMap = entity.getLayers();
//        CodeBlock throwExceptionCodeBlock = poetHelperService.buildFindByIdCodeBlock(entity);

        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(), "daoClass");
        FieldSpec daoField = FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec constructor = poetHelperService.buildConstructor(entity, "serviceImplClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("SERVICE_IMPL")).findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = new ArrayList<>();
        serviceImplLayerOptional.get().getMethods().forEach(method -> {
            List<ParameterSpec> parameters = new ArrayList<>();
            method.getMethodSignature().getParameters().forEach(parameter -> {
                FieldTypeNameWrapper typeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(), "daoClass");
                parameters.add(
                        ParameterSpec
                                .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                                .build());
            });

            FieldTypeNameWrapper returnTypeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(), method.getMethodSignature().getReturns());

            methods.add(MethodSpec.methodBuilder(method.getType())
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .returns(returnTypeNameWrapper.getTypeName())
                    .build());
        });

        return TypeSpec
                .classBuilder(entity.getLayers().get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(daoField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
