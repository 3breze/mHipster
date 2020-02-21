package com.oul.mHipster.service.model.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.wrapper.CodeBlockStatement;
import com.oul.mHipster.layerconfig.wrapper.StatementArg;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class EntityManagerServiceImpl implements EntityManagerService {

    private Map<String, Map<String, TypeWrapper>> layerModelFactory;
    private Map<String, Map<Integer, CodeBlockStatement>> methodStatementFactory;
    private List<String> special_entity_name_keys = List.of("entityName", "relationName", "stringbuilder");

    @Override
    public void setLayerModelFactory(LayerModelWrapper layerModelWrapper) {
        this.layerModelFactory = layerModelWrapper.getLayerClassModelMap();
        this.methodStatementFactory = layerModelWrapper.getMethodStatementMap();
    }

    /**
     * Method overriding za pronalazenje method parametara (nikad nece biti generic polja).
     * TypeName-ovi klasa iz modela (ne ukljucuje dependencies i javine klase)
     */
    @Override
    public TypeWrapper getProperty(String entityName, String layerName) {
        return Optional.ofNullable(layerModelFactory.get(entityName).get(layerName))
                .orElseThrow(() -> new ConfigurationErrorException("Can't find property for entity name or layer name!"));
    }

    /**
     * Nazivi polja su kljucevi nestovane mape. Nad mapa za kljuceve ima "dependencies" (gde spadaju QueryDSL i Sping Data
     * paketi) i nazive clasa entita u projektu ("TechnicalData", "Company"...).
     * Provera fieldName-a je nad kljucevima nestovanih klasa pod dva kljuca: naziva klase datog entiteta i dependencies.
     */
    @Override
    public TypeWrapper getProperty(String entityName, String typeArgument, String instanceName) {
        TypeWrapper entityBasedClass = layerModelFactory.get(entityName).get(typeArgument);
        if (entityBasedClass == null) {
            TypeWrapper dependencyBasedClass = layerModelFactory.get("dependencies").get(typeArgument);
            if (dependencyBasedClass == null) {
                if (Character.isLowerCase(typeArgument.charAt(0))) {
                    TypeName typeName = getPrimitiveTypeName(typeArgument);
                    return new TypeWrapper(typeName, instanceName);
                }
                if (typeArgument.equals("List")) {
                    return new TypeWrapper(ClassName.get("java.util", typeArgument), instanceName);
                }
                return new TypeWrapper(ClassName.get("java.lang", typeArgument), instanceName);
            }
            return dependencyBasedClass;
        }
        return entityBasedClass;
    }


    @Override
    public CodeBlockStatement computeStatement(String helperName, Integer statementIdx, Map<String, String> classNamesMap) {
        CodeBlockStatement result = methodStatementFactory.get(helperName).get(statementIdx);
        result.setResponseArgs(prepareStatementResponse(result.getRequestArgs(), classNamesMap));
        return result;
    }

    private Object[] prepareStatementResponse(List<StatementArg> statementArgs, Map<String, String> classNames) {

        return statementArgs.stream().map(statementArg -> {
            String entityNameKey = statementArg.getEntityNameKey();
            String className = classNames.get(entityNameKey);

            if (special_entity_name_keys.contains(entityNameKey)) {
                return Objects.nonNull(statementArg.getStringOperation()) ?
                        getStringOperationFunc(statementArg.getStringOperation())
                                .apply(className) : className;
            }

            TypeWrapper type = getProperty(className, statementArg.getClassLayer(), null);

            if (!statementArg.isClazz())
                return Objects.nonNull(statementArg.getStringOperation()) ?
                        getStringOperationFunc(statementArg.getStringOperation())
                                .apply(type.getInstanceName()) : type.getInstanceName();

            return type.getTypeName();
        }).toArray(Object[]::new);
    }

    private Function<String, String> getStringOperationFunc(String functionName) {
        switch (functionName) {
            case "capitalizeField":
                return ClassUtils::capitalizeField;
        }
        throw new ConfigurationErrorException("Unknown string operation keyword from helper method config!");
    }

    private TypeName getPrimitiveTypeName(String typeArgument) {
        TypeName result = null;
        switch (typeArgument) {
            case "void":
                result = TypeName.VOID;
                break;
            case "boolean":
                result = TypeName.BOOLEAN;
                break;
            case "byte":
                result = TypeName.BYTE;
                break;
            case "short":
                result = TypeName.SHORT;
                break;
            case "int":
                result = TypeName.INT;
                break;
            case "long":
                result = TypeName.LONG;
                break;
            case "char":
                result = TypeName.CHAR;
                break;
            case "float":
                result = TypeName.FLOAT;
                break;
            case "double":
                result = TypeName.DOUBLE;
                break;
        }
        return result;
    }
}
