package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.Parameter;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.layersConfig.wrapper.LayerInfoWrapper;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.RelationType;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.PoetHelperService;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private PoetHelperService poetHelperService;
    private LayersConfig layersConfig;

    public GenerateServiceImplClassStrategy() {
        this.poetHelperService = new PoetHelperService();
        this.layersConfig = Util.getValue();
    }

    @Override
    public TypeSpec generate(Entity entity) {
        Map<String, LayerClass> layerMap = entity.getLayers();
        Map<String, TypeName> typeNameMap = poetHelperService.createTypeNames(entity);
//        CodeBlock throwExceptionCodeBlock = poetHelperService.buildFindByIdCodeBlock(entity);

        List<FieldSpec> fieldSpecList = new ArrayList<>();
        List<ParameterSpec> parameterSpecsList = new ArrayList<>();
        FieldSpec daoField = FieldSpec
                .builder(typeNameMap.get("daoClass"), layerMap.get(LayerName.DAO.toString()).getClassName())
                .addModifiers(Modifier.PRIVATE)
                .build();
        fieldSpecList.add(daoField);

        entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                        ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .forEach(attribute -> {
                    fieldSpecList.add(FieldSpec
                            .builder(typeNameMap.get("serviceImplClass"), layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                            .addModifiers(Modifier.PRIVATE)
                            .build());
                    parameterSpecsList.add(ParameterSpec
                            .builder(typeNameMap.get("serviceImplClass"), layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                            .build());
                });

        // CONSTRUCTOR
        CodeBlock.Builder builder = CodeBlock.builder();
        fieldSpecList.forEach(cb -> builder.addStatement("this.$N = $N", cb, cb));

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addAnnotation(Autowired.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();


        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("SERVICE_IMPL")).findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = new ArrayList<>();
        serviceImplLayerOptional.get().getMethods().forEach(method -> {
            List<ParameterSpec> parameters = new ArrayList<>();
            method.getMethodSignature().getParameters().forEach(parameter -> {
                parameters.add(ParameterSpec
                        .builder(typeNameMap.get(parameter.getName()), parameter.getName())
                        .build());
            });

            methods.add(MethodSpec.methodBuilder(method.getType())
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .returns(typeNameMap.get(method.getMethodSignature().getReturns()))
                    .build());
        });

        return TypeSpec
                .classBuilder(layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(daoField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
