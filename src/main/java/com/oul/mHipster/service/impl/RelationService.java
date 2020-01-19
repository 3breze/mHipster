package com.oul.mHipster.service.impl;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class RelationService {

    Attribute findRelation(Field field, Class clazz) {
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        Annotation annO2M = field.getAnnotation(OneToMany.class);
        Annotation annO2O = field.getAnnotation(OneToOne.class);
        Annotation annM2O = field.getAnnotation(ManyToOne.class);

        return Stream.of(annM2M, annO2M, annO2O, annM2O).filter(Objects::nonNull)
                .map(annotation -> resolveRelation(annotation, field, clazz))
                .findFirst()
                .orElse(new Attribute(field.getType(), field.getName()));
    }

    private Attribute resolveRelation(Annotation annotation, Field field, Class clazz) {
        Class<? extends Annotation> type = annotation.annotationType();

        for (Method method : type.getDeclaredMethods()) {
            Object value = null;

            try {
                value = method.invoke(annotation, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (method.getName().equals("mappedBy") && !value.equals("")) {

                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> relationDomainClass = (Class<?>) genericType.getActualTypeArguments()[0];
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(field.getType()),
                        relationDomainClass.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            } else if (method.getName().equals("mappedBy") && value.equals("")) {

                Class<?> typeArgument = ReflectionUtil.resolveTypeArgument(field);
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(typeArgument),
                        clazz.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            }
        }
        return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(field.getType()),
                clazz.getSimpleName(), RelationType.MANYTOONE);
    }

    // maybe?
    public List<RelationAttribute> findRelationAttributes(Entity entity) {
        return entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.toList());
    }
}
