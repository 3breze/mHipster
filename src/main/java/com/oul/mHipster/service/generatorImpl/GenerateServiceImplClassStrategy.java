package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.*;
import com.oul.mHipster.service.impl.AttributeService;
import com.oul.mHipster.service.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.impl.LayerBuilderHelperServiceImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private LayersConfig layersConfig;
    private EntityManagerService entityManagerService;
    private JPoetHelperService jPoetHelperService;
    private AttributeService attributeService;
    private LayerBuilderHelperService layerBuilderHelperService;

    public GenerateServiceImplClassStrategy() {
        this.layersConfig = Util.getValue();
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
        this.layerBuilderHelperService = new LayerBuilderHelperServiceImpl();
    }

    @Override
    public TypeSpec generate(Entity entity) {
        CodeBlock throwExceptionCodeBlock = jPoetHelperService.buildFindByIdCodeBlock(entity);

        // Ovo ce biti izmesteno npr. findFieldsForEntity
        List<RelationAttribute> relationAttributes = attributeService.findRelationAttributes(entity);
        List<FieldSpec> fieldSpecList = relationAttributes.stream().map(attribute -> {
            FieldTypeNameWrapper typeNameWrapper = entityManagerService.getProperty(attribute.getClassSimpleName(),
                    "serviceClass");
            return FieldSpec
                    .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build();
        }).collect(Collectors.toList());

        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "daoClass");
        fieldSpecList.add(FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build());

        // dependencyClass -> needs DAO dependency
        MethodSpec constructor = jPoetHelperService.buildConstructor(entity, fieldSpecList, "daoClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("SERVICE_IMPL"))
                .findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());
            CodeBlock methodBody = layerBuilderHelperService.processMethodBody(entity, method.getMethodBody());
            System.out.println("methodBody = " + methodBody);
            List<ParameterSpec> parameters = layerBuilderHelperService.resolveParameters(entity, method);
            TypeName returnTypeName = attributeService.getReturnTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns());

            return methodBuilder
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
//                    .addCode()
                    .returns(returnTypeName)
                    .build();
        }).collect(Collectors.toList());

        FieldTypeNameWrapper serviceTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "serviceClass");

        return TypeSpec
                .classBuilder(entity.getLayers().get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addSuperinterface(serviceTypeNameWrapper.getTypeName())
                .addFields(fieldSpecList)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
