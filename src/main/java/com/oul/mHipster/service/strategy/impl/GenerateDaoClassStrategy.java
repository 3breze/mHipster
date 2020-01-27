package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.oul.mHipster.service.helper.MethodBuilderService;
import com.oul.mHipster.service.helper.impl.AttributeBuilderService;
import com.oul.mHipster.service.helper.impl.MethodBuilderServiceImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateDaoClassStrategy implements GenerateLayerStrategy {

    private LayersConfig layersConfig;
    private EntityManagerService entityManagerService;
    private MethodBuilderService methodBuilderService;
    private AttributeBuilderService attributeBuilderService;

    public GenerateDaoClassStrategy() {
        this.layersConfig = Util.getValue();
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.attributeBuilderService = new AttributeBuilderService();
        this.methodBuilderService = new MethodBuilderServiceImpl();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("DAO"))
                .findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());

            List<ParameterSpec> parameters = methodBuilderService.getMethodParameters(entity, method, LayerName.SERVICE.name());

            FieldTypeNameWrapper returnTypeName = attributeBuilderService.getReturnTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns(), null);

            return methodBuilder
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameters(parameters)
                    .returns(returnTypeName.getTypeName())
                    .build();
        }).collect(Collectors.toList());

        FieldTypeNameWrapper jpaTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "JpaRepository", null);

        FieldTypeNameWrapper dslPredicateTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "QuerydslPredicateExecutor", null);

        ClassName boxedLong = ClassName.get("java.lang", "Long");

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.DAO.toString());

        TypeSpec typeSpec = TypeSpec
                .interfaceBuilder(classNamingInfo.getClassName())
                .addSuperinterface(ParameterizedTypeName.get((ClassName) jpaTypeNameWrapper.getTypeName(),
                        domainTypeNameWrapper.getTypeName(), boxedLong))
                .addSuperinterface(ParameterizedTypeName.get((ClassName) dslPredicateTypeNameWrapper.getTypeName(),
                        domainTypeNameWrapper.getTypeName()))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
