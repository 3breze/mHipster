package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

public class GenerateDaoClassStrategy implements GenerateLayerStrategy {

    private EntityManagerService entityManagerService;

    public GenerateDaoClassStrategy() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        FieldTypeNameWrapper domainTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "domainClass");

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
                .build();

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
