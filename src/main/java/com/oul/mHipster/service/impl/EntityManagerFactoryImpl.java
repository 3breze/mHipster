package com.oul.mHipster.service.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        createDependenciesTypeNames();
        createRelationTypeNames();
    }

    @Override
    public Map<String, Map<String, FieldTypeNameWrapper>> getMetamodel() {
        return metamodel;
    }

    /**
     * Method overriding za pronalazenje method parametara (nikad nece biti generic polja).
     * TypeName-ovi klasa iz modela (ne ukljucuje dependencies i javine klase)
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String layerName) {
        return Optional.ofNullable(metamodel.get(entityName).get(layerName))
                .orElseThrow(() -> new ConfigurationErrorException("Reading configuration failed!"));
    }

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * fieldName kao u slucaju parametara methoda)
     */
    @Override
    public TypeName getReturnTypeName(String entityName, String fieldName) {
        return fieldName.contains("<") ? parameterizedTypeTokenSplit(fieldName, entityName) :
                getProperty(entityName, fieldName, null).getTypeName();
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private TypeName parameterizedTypeTokenSplit(String genericField, String entityName) {
        FieldTypeNameWrapper genericType = getProperty(entityName, genericField.substring(0, genericField.indexOf("<")),
                null);
        FieldTypeNameWrapper typeArgument = getProperty(entityName, genericField.substring(genericField.indexOf("<") + 1,
                genericField.indexOf(">")), null);

        return ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
    }

    /**
     * Nazivi polja su kljucevi nestovane mape. Nad mapa za kljuceve ima "dependencies" (gde spadaju QueryDSL i Sping Data
     * paketi) i nazive clasa entita u projektu ("TechnicalData", "Company"...).
     * Provera fieldName-a je nad kljucevima nestovanih klasa pod dva kljuca: naziva klase datog entiteta i dependencies.
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String typeArgument, String instanceName) {
        FieldTypeNameWrapper entityBasedClass = metamodel.get(entityName).get(typeArgument);
        if (entityBasedClass == null) {
            FieldTypeNameWrapper dependencyBasedClass = metamodel.get("dependencies").get(typeArgument);
            if (dependencyBasedClass == null)
                return new FieldTypeNameWrapper(ClassName.bestGuess(typeArgument), instanceName);
            return dependencyBasedClass;
        }
        return entityBasedClass;
    }

    @Override
    public List<RelationAttribute> findRelationAttributes(Entity entity) {
        return entity.getAttributes().parallelStream()
                .filter(RelationAttribute.class::isInstance)
                .filter(attribute -> ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOONE) ||
                        ((RelationAttribute) attribute).getRelationType().equals(RelationType.MANYTOMANY) &&
                                ((RelationAttribute) attribute).getOwner().equals(entity.getClassName()))
                .map(attribute -> (RelationAttribute) attribute)
                .collect(Collectors.toList());
    }

    public void createDependenciesTypeNames() {
        Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

        typeNameMap.put("Pageable", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Pageable"), "pageable"));
        typeNameMap.put("Page", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Page"), "page"));
        typeNameMap.put("PageImpl", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "PageImpl"), "pageImpl"));
        typeNameMap.put("Predicate", new FieldTypeNameWrapper(
                ClassName.get("import com.querydsl.core.types", "Predicate"), "predicate"));

        metamodel.put("dependencies", typeNameMap);
    }

    @Override
    public void createRelationTypeNames() {
        this.sourceDomainLayer.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layerMap = entity.getLayers();
            Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

            typeNameMap.put("domainClass", new FieldTypeNameWrapper(
                    ClassName.get(entity.getPackageName(), entity.getClassName()), entity.getInstanceName()));

            ClassNamingInfo requestInfo = layerMap.get(LayerName.REQUEST_DTO.toString());
            typeNameMap.put("requestClass", new FieldTypeNameWrapper(
                    ClassName.get(requestInfo.getPackageName(), requestInfo.getClassName()), requestInfo.getInstanceName()));

            ClassNamingInfo responseInfo = layerMap.get(LayerName.RESPONSE_DTO.toString());
            typeNameMap.put("responseClass", new FieldTypeNameWrapper(
                    ClassName.get(responseInfo.getPackageName(), responseInfo.getClassName()), responseInfo.getInstanceName()));

            ClassNamingInfo daoInfo = layerMap.get(LayerName.DAO.toString());
            typeNameMap.put("daoClass", new FieldTypeNameWrapper(
                    ClassName.get(daoInfo.getPackageName(), daoInfo.getClassName()), daoInfo.getInstanceName()));

            ClassNamingInfo apiInfo = layerMap.get(LayerName.API.toString());
            typeNameMap.put("apiClass", new FieldTypeNameWrapper(
                    ClassName.get(apiInfo.getPackageName(), apiInfo.getClassName()), apiInfo.getInstanceName()));

            ClassNamingInfo serviceInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceClass", new FieldTypeNameWrapper(
                    ClassName.get(serviceInfo.getPackageName(), serviceInfo.getClassName()), serviceInfo.getInstanceName()));

            ClassNamingInfo serviceImplInfo = layerMap.get(LayerName.SERVICE.toString());
            typeNameMap.put("serviceImplClass", new FieldTypeNameWrapper(
                    ClassName.get(serviceImplInfo.getPackageName(), serviceImplInfo.getClassName()), serviceImplInfo.getInstanceName()));

            metamodel.put(entity.getClassName(), typeNameMap);
        });
    }
}
