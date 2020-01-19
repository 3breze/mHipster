package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;

public interface EntityManagerFactory {

    void createEntityManager(RootEntityModel rootEntityModel);

    TypeName getReturnTypeName(String entityName, String fieldName);

    FieldTypeNameWrapper getProperty(String entityName, String layerName);

    FieldTypeNameWrapper getProperty(String entityName, String layerName, String fieldName);

    List<RelationAttribute> findRelationAttributes(Entity entity);
}
