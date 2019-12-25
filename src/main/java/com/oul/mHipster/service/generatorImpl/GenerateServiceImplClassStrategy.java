package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.Parameter;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.layersConfig.wrapper.LayerInfoWrapper;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
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
        CodeBlock throwExceptionCodeBlock = poetHelperService.buildFindByIdCodeBlock(entity);


        FieldSpec daoField = FieldSpec
                .builder(typeNameMap.get("daoClass"), layerMap.get(LayerName.DAO.toString()).getClassName())
                .addModifiers(Modifier.PRIVATE)
                .build();


        List<MethodSpec> methods = new ArrayList<>();
        layersConfig.getLayers().forEach(layer -> layer.getMethods().forEach(method -> {
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
        }));

        TypeSpec serviceClass = TypeSpec
                .classBuilder(layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(daoField)
//                .addMethod(constructor)
                .addMethods(methods)
                .build();

//
//
//        // CONSTRUCTOR
//        List<ParameterSpec> parameterSpecsList = entity.getAttributes().stream().map(entry -> ParameterSpec
//                .builder(ClassName.bestGuess(entry.getType().getName()), entry.getValue())
//                .build()).collect(Collectors.toList());
//
//        CodeBlock.Builder builder = CodeBlock.builder();
//        typeToValue.values().forEach(cb -> builder.addStatement("this.$N = $N", cb, cb));
//
//
//
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addAnnotation(Autowired.class)
//                .addModifiers(Modifier.PUBLIC)
//                .addParameters(parameterSpecsList)
//                .addCode(builder.build())
//                .build();
//
//
//        TypeSpec serviceClass = TypeSpec
//                .classBuilder("CustomerServiceImpl")
//                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Service.class)
//                .addField(customerDao)
//                .addMethod(constructor)
//                .addMethod(MethodSpec
//                        .methodBuilder("save")
//                        .addCode(throwExceptionCodeBlock)
//                        .addModifiers(Modifier.PUBLIC)
//                        .addAnnotation(Override.class)
//                        .addParameter(param)
//                        .addStatement(codeBlock2)
//                        .returns(responseDtoClass)
//                        .build())
//                .build();

        return null;
    }
}
