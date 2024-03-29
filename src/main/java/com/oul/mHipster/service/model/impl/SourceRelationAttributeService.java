package com.oul.mHipster.service.model.impl;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.RelationType;
import com.oul.mHipster.util.ClassUtils;
import com.oul.mHipster.util.ReflectionUtil;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class SourceRelationAttributeService {

    private static final String MAPPED_BY_ANN_NAME = "mappedBy";
    private static final String EMPTY_MAPPED_BY_ANN_VALUE = "";

    protected Attribute findRelation(Field field, Class clazz) {
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        Annotation annO2M = field.getAnnotation(OneToMany.class);
        Annotation annO2O = field.getAnnotation(OneToOne.class);
        Annotation annM2O = field.getAnnotation(ManyToOne.class);

        return Stream.of(annM2M, annO2M, annO2O, annM2O)
                .filter(Objects::nonNull)
                .map(annotation -> resolveRelation(annotation, field, clazz))
                .findFirst()
                .orElse(new Attribute(field.getType(), field.getName()));
    }

    private Attribute resolveRelation(Annotation annotation, Field field, Class clazz) {
        Class<? extends Annotation> type = annotation.annotationType();

        for (Method method : type.getDeclaredMethods()) {
            Object value = ReflectionUtil.methodInvoker(method, annotation);

            if (method.getName().equals(MAPPED_BY_ANN_NAME) && !value.equals(EMPTY_MAPPED_BY_ANN_VALUE)) {
                Class<?> typeArgument = ReflectionUtil.resolveTypeArgument(field);
                Class<?> relationDomainClass = ReflectionUtil.resolveParameterizedType(field);
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(typeArgument),
                        relationDomainClass.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));

            } else if (method.getName().equals(MAPPED_BY_ANN_NAME) && value.equals(EMPTY_MAPPED_BY_ANN_VALUE)) {
                Class<?> typeArgument = ReflectionUtil.resolveTypeArgument(field);
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(typeArgument),
                        clazz.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            }
        }
        return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(field.getType()),
                clazz.getSimpleName(), RelationType.MANYTOONE);
    }
}
