package com.oul.mHipster.service;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.SourceDomainLayer;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;

import java.util.List;
import java.util.Map;

public interface EntityManagerFactory {

    void createEntityManager(SourceDomainLayer sourceDomainLayer);

    Map<String, Map<String, FieldTypeNameWrapper>> getMetamodel();

    FieldTypeNameWrapper getProperty(String entityName, String layerName);

    FieldTypeNameWrapper getProperty(String entityName, String layerName, String fieldName);

    void createRelationTypeNames();

    void createDependenciesTypeNames();

    List<Attribute> findRelationAttributes(Entity entity);

    Boolean checkIfDomain(String clazzName);
}
