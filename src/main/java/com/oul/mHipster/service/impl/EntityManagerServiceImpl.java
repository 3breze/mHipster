package com.oul.mHipster.service.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.EntityManagerService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Map;
import java.util.Optional;

public class EntityManagerServiceImpl implements EntityManagerService {

    private Map<String, Map<String, FieldTypeNameWrapper>> layerModel;

    @Override
    public void setLayerModel(LayerModelWrapper layerModelWrapper) {
        this.layerModel = layerModelWrapper.getLayerClassModel();
    }

    /**
     * Method overriding za pronalazenje method parametara (nikad nece biti generic polja).
     * TypeName-ovi klasa iz modela (ne ukljucuje dependencies i javine klase)
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String layerName) {
        return Optional.ofNullable(layerModel.get(entityName).get(layerName))
                .orElseThrow(() -> new ConfigurationErrorException("Reading configuration failed!"));
    }

    /**
     * Nazivi polja su kljucevi nestovane mape. Nad mapa za kljuceve ima "dependencies" (gde spadaju QueryDSL i Sping Data
     * paketi) i nazive clasa entita u projektu ("TechnicalData", "Company"...).
     * Provera fieldName-a je nad kljucevima nestovanih klasa pod dva kljuca: naziva klase datog entiteta i dependencies.
     */
    @Override
    public FieldTypeNameWrapper getProperty(String entityName, String typeArgument, String instanceName) {
        FieldTypeNameWrapper entityBasedClass = layerModel.get(entityName).get(typeArgument);
        if (entityBasedClass == null) {
            FieldTypeNameWrapper dependencyBasedClass = layerModel.get("dependencies").get(typeArgument);
            if (dependencyBasedClass == null) {
                if (typeArgument.equals("void")) return new FieldTypeNameWrapper(TypeName.VOID, instanceName);
                return new FieldTypeNameWrapper(ClassName.get("java.lang", typeArgument), instanceName);
            }
            return dependencyBasedClass;
        }
        return entityBasedClass;
    }
}