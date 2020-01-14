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

    /*
     * ?
     * FieldName i layerName kao parametri za kljuceve nestovanih mapa u metamodelu se mesaju ali se misli na isto
     * Iz perspektive typeName-a za dao smislenije da se parametar zove layerName, ali iz perspektive return type-a
     * on trazi kljuc pod parametrom fieldName.
     * GetPropety i checkIfInModel jako slicni, ali u prvom ne mogu da u else-u uradim BestGuess jer mi treba parametar
     * name da vratim u tom slucaju (prakticno getProperty uvek mora da nadje FieldType domenske klase, exception je
     * cisto reda radi tu). CheckIfInModelu moze da ide dalje pretragu putem BestGuess-a jer ga trazi samo za potrebe
     * popunjavanja typeArgument-a u generic tipu te mu ne treba parametar za ime.
     */

    /**
     * Method overriding za pronalazenje method parametara (nikad nece biti generic polja).
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String layerName) {
        return Optional.ofNullable(metamodel.get(entityName).get(layerName))
                .orElseThrow(() -> new ConfigurationErrorException("Reading configuration failed!"));
    }

    /**
     * Ako se radi o polju koja nisu domenske klase (eg. "Long id, String name")
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String layerName, String fieldName) {
        return new FieldTypeNameWrapper(ClassName.bestGuess(layerName), fieldName);
    }

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * fieldName kao u slucaju parametara methoda)
     */
    @Override
    public TypeName getReturnTypeName(String entityName, String fieldName) {
        return fieldName.contains("<") ? parameterizedTypeTokenSplit(fieldName, entityName) : checkIfInModel(entityName, fieldName).getTypeName();
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private TypeName parameterizedTypeTokenSplit(String genericField, String entityName) {
        System.out.println("parameterized: " + genericField);
        FieldTypeNameWrapper genericType = checkIfInModel(entityName, genericField.substring(0, genericField.indexOf("<")));
        FieldTypeNameWrapper typeArgument = checkIfInModel(entityName, genericField.substring(genericField.indexOf("<") + 1,
                genericField.indexOf(">")));

        return ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
    }

    /**
     * Nazivi polja su kljucevi nestovane mape. Nad mapa za kljuceve ima "dependencies" (gde spadaju QueryDSL i Sping Data
     * paketi) i nazive clasa entita u projektu ("TechnicalData", "Company"...).
     * Provera fieldName-a je nad kljucevima nestovanih klasa pod dva kljuca: naziva klase datog entiteta i dependencies.
     */
    private FieldTypeNameWrapper checkIfInModel(String entityName, String fieldName) {
        System.out.println("checkIfInModel: " + fieldName);
        System.out.println(metamodel.get(entityName).get(fieldName));
        FieldTypeNameWrapper field = metamodel.get(entityName).get(fieldName);
        if (field != null) {
            return field;
        } else {
            return new FieldTypeNameWrapper(ClassName.bestGuess(fieldName), fieldName);
        }
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

    public void createDependenciesTypeNames() {
        Map<String, FieldTypeNameWrapper> typeNameMap = new HashMap<>();

        typeNameMap.put("pageable", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Pageable"), "pageable"));
        typeNameMap.put("page", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "Page"), "page"));
        typeNameMap.put("pageImpl", new FieldTypeNameWrapper(
                ClassName.get("org.springframework.data.domain", "PageImpl"), "pageImpl"));
        typeNameMap.put("predicate", new FieldTypeNameWrapper(
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
            typeNameMap.put("requestClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
                            layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("responseClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.RESPONSE_DTO.toString()).getPackageName(),
                            layerMap.get(LayerName.RESPONSE_DTO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("daoClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.DAO.toString()).getPackageName(),
                            layerMap.get(LayerName.DAO.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("apiClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.API.toString()).getPackageName(),
                            layerMap.get(LayerName.API.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("serviceClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.SERVICE.toString()).getPackageName(),
                            layerMap.get(LayerName.SERVICE.toString()).getClassName()), entity.getInstanceName()));
            typeNameMap.put("serviceImplClass", new FieldTypeNameWrapper(
                    ClassName.get(layerMap.get(LayerName.SERVICE_IMPL.toString()).getPackageName(),
                            layerMap.get(LayerName.SERVICE_IMPL.toString()).getClassName()), entity.getInstanceName()));

            metamodel.put(entity.getClassName(), typeNameMap);
        });
    }
}
