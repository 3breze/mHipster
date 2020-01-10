package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityManagerFactoryImpl implements EntityManagerFactory {

    private static final EntityManagerFactory instance = new EntityManagerFactoryImpl();
    private SourceDomainLayer sourceDomainLayer;
    private Map<String, Map<String, FieldTypeNameWrapper>> metamodel = new HashMap<>();

    private EntityManagerFactoryImpl() {
    }

    public static EntityManagerFactory getInstance() {
        return instance;
    }

    @Override
    public void createEntityManager(SourceDomainLayer sourceDomainLayer) {
        this.sourceDomainLayer = sourceDomainLayer;
        createTypeNames();
    }

    @Override
    public Map<String, Map<String, FieldTypeNameWrapper>> getMetamodel() {
        return metamodel;
    }

    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String layerName) {
        return checkIfDomain(entityName) ? metamodel.get(entityName).get(layerName) : new FieldTypeNameWrapper(ClassName.bestGuess(layerName), "nazivPoljaVrvUvekIdSamo");
    }

    @Override
    public Boolean checkIfDomain(String entityName) {
        return metamodel.containsKey(entityName);
    }

    @Override
    public List<Attribute> findRelationAttributes(Entity entity) {
        return entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .collect(Collectors.toList());
    }

    @Override
    public void createTypeNames() {
        this.sourceDomainLayer.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layerMap = entity.getLayers();
            Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();
            typeNameMap.put("domainClass", new FieldTypeNameWrapper(ClassName.get(entity.getPackageName(), entity.getClassName()), entity.getInstanceName()));
            typeNameMap.put("requestClazz", new FieldTypeNameWrapper(ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
                    layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("responseClazz", new FieldTypeNameWrapper(ClassName.get(layerMap.get(LayerName.RESPONSE_DTO.toString()).getPackageName(),
                    layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("daoClass", new FieldTypeNameWrapper(ClassName.get(layerMap.get(LayerName.DAO.toString()).getPackageName(),
                    layerMap.get(LayerName.DAO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("apiClass", new FieldTypeNameWrapper(ClassName.get(layerMap.get(LayerName.API.toString()).getPackageName(),
                    layerMap.get(LayerName.API.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("serviceImplClass", new FieldTypeNameWrapper(ClassName.get(layerMap.get(LayerName.SERVICE_IMPL.toString()).getPackageName(),
                    layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName()), entity.getInstanceName()));
            metamodel.put(entity.getClassName(), typeNameMap);
        });
    }
}
