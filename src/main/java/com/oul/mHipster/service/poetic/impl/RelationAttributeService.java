package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.RelationType;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
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

    public List<RelationAttribute> findRelationAttributes(Entity entity) {
        return entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.toList());
    }

    public List<FieldSpec> getRelationAttributeFieldSpecList(Entity entity) {
        Map<Boolean, List<RelationAttribute>> parameterizedPartition = entity.getAttributes().stream()
                .filter(RelationAttribute.class::isInstance)
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.partitioningBy(attribute -> ReflectionUtil.isParameterizedType(attribute.getType())));

        ArrayList<FieldSpec> target = new ArrayList<>();
        target.ensureCapacity(parameterizedPartition.get(true).size() + parameterizedPartition.get(false).size());

        parameterizedPartition.get(true).stream()
                .map(relationAttribute -> {
                    FieldTypeNameWrapper fieldSpec = entityManagerService.getProperty(relationAttribute.getTypeArgument(),
                            "domainClass", relationAttribute.getFieldName());

                    String collectionInterfaceExtracted = ClassUtils.getCollectionInterface(relationAttribute.getType().toString());
                    TypeName parameterized = ParameterizedTypeName.get(ClassName.bestGuess(collectionInterfaceExtracted),
                            fieldSpec.getTypeName());
                    return FieldSpec.
                            builder(parameterized, fieldSpec.getInstanceName())
                            .addModifiers(Modifier.PRIVATE)
                            .build();
                }).forEachOrdered(target::add);

        parameterizedPartition.get(false).stream()
                .map(relationAttribute -> {
                    FieldTypeNameWrapper fieldSpec = entityManagerService.getProperty(entity.getClassName(),
                            relationAttribute.getTypeArgument(), relationAttribute.getFieldName());
                    return FieldSpec.
                            builder(fieldSpec.getTypeName(), fieldSpec.getInstanceName())
                            .addModifiers(Modifier.PRIVATE)
                            .build();
                }).forEachOrdered(target::add);

        return target;
    }
}
