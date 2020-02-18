package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.RelationType;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.EntityManagerFactory;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.util.ClassUtils;
import com.oul.mHipster.util.ReflectionUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RelationAttributeService {

    EntityManagerService entityManagerService;

    RelationAttributeService() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    public List<FieldSpec> buildRelationFieldSpecList(Entity entity) {

        List<RelationAttribute> relationAttributes = entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.toList());

        return relationAttributes.stream().map(attribute -> {
            TypeWrapper serviceType = entityManagerService.getProperty(attribute.getTypeArgument(),
                    "serviceClass");
            return FieldSpec
                    .builder(serviceType.getTypeName(), serviceType.getInstanceName())
                    .addModifiers(Modifier.PRIVATE)
                    .build();
        }).collect(Collectors.toList());
    }

    Map<Boolean, List<RelationAttribute>> partitionParameterizedRelationAttributes(Entity entity) {
        return entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.partitioningBy(attribute -> ReflectionUtil.isParameterizedType(attribute.getType())));
    }

    public List<FieldSpec> getRelationAttributeFieldSpecList(Entity entity, LayerName layerName) {
        Map<Boolean, List<RelationAttribute>> parameterizedPartition = entity.getAttributes().stream()
                .filter(RelationAttribute.class::isInstance)
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.partitioningBy(attribute -> ReflectionUtil.isParameterizedType(attribute.getType())));

        ArrayList<FieldSpec> target = new ArrayList<>();
        target.ensureCapacity(parameterizedPartition.get(true).size() + parameterizedPartition.get(false).size());

        parameterizedPartition.get(true).stream()
                .map(relationAttribute -> {
                    String collectionInterfaceExtracted = ClassUtils.getCollectionInterface(relationAttribute.getType().toString());
                    TypeWrapper responseType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                            "responseClass", relationAttribute.getFieldName());
                    boolean flag = false;
                    if (layerName.equals(LayerName.RESPONSE_DTO)) flag = true;

                    TypeName parameterized = ParameterizedTypeName.get(ClassName.bestGuess(collectionInterfaceExtracted),
                            flag ? responseType.getTypeName() : ClassName.get("java.lang", "Long"));
                    return FieldSpec.
                            builder(parameterized, flag ? relationAttribute.getFieldName() : relationAttribute.getFieldName() + "Ids")
                            .addModifiers(Modifier.PRIVATE)
                            .build();
                }).forEachOrdered(target::add);

        parameterizedPartition.get(false).stream()
                .map(relationAttribute -> {
                    TypeWrapper fieldSpec = entityManagerService.getProperty(entity.getClassName(),
                            relationAttribute.getTypeArgument(), relationAttribute.getFieldName());
                    TypeWrapper responseType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                            "responseClass", relationAttribute.getFieldName());
                    TypeWrapper relationDomainType = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                            "domainClass", relationAttribute.getFieldName());
                    boolean flag = false;
                    if (layerName.equals(LayerName.RESPONSE_DTO)) flag = true;
                    return FieldSpec.
                            builder(flag ? responseType.getTypeName() : ClassName.get("java.lang", "Long"),
                                    flag ? relationDomainType.getInstanceName() : fieldSpec.getInstanceName() + "Id")
                            .addModifiers(Modifier.PRIVATE)
                            .build();
                }).forEachOrdered(target::add);

        return target;
    }
}
