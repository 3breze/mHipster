package com.oul.mHipster.service.model.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.wrapper.StatementArg;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.service.model.EntityManagerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EntityManagerServiceImpl implements EntityManagerService {

    private Map<String, Map<String, TypeWrapper>> layerModel;
    private Map<String, Map<String, List<StatementArg>>> methodStatementFactory = new HashMap<>();
    private static final String REGEX = "\\$\\(L|T)";

    @Override
    public void setLayerModel(LayerModelWrapper layerModelWrapper) {
        this.layerModel = layerModelWrapper.getLayerClassModel();
        initStatementArgs();
    }

    /**
     * Method overriding za pronalazenje method parametara (nikad nece biti generic polja).
     * TypeName-ovi klasa iz modela (ne ukljucuje dependencies i javine klase)
     */
    @Override
    public TypeWrapper getProperty(String entityName, String layerName) {
        return Optional.ofNullable(layerModel.get(entityName).get(layerName))
                .orElseThrow(() -> new ConfigurationErrorException("Reading configuration failed!"));
    }

    /**
     * Nazivi polja su kljucevi nestovane mape. Nad mapa za kljuceve ima "dependencies" (gde spadaju QueryDSL i Sping Data
     * paketi) i nazive clasa entita u projektu ("TechnicalData", "Company"...).
     * Provera fieldName-a je nad kljucevima nestovanih klasa pod dva kljuca: naziva klase datog entiteta i dependencies.
     */
    @Override
    public TypeWrapper getProperty(String entityName, String typeArgument, String instanceName) {
        TypeWrapper entityBasedClass = layerModel.get(entityName).get(typeArgument);
        if (entityBasedClass == null) {
            TypeWrapper dependencyBasedClass = layerModel.get("dependencies").get(typeArgument);
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


    private void initStatementArgs() {

        methodStatementFactory.put("buildFindManyRelationCodeBlock",
                Map.of("$T<$T> $LList = $L.findByIds($L.get$LListIds())",
                        List.of(new StatementArg("List", "type"),
                                new StatementArg("domainClass", "type"),
                                new StatementArg("domainClass", "instance"),
                                new StatementArg("serviceClass", "instance"),
                                new StatementArg("requestClass", "instance"),
                                new StatementArg("domainClass", "instance", ClassUtils::capitalizeField)),
                        "$L.set$LList($LList)",
                        List.of(new StatementArg("domainClass", "instance"),
                                new StatementArg("domainClass", "instance"),
                                new StatementArg("domainClass", "instance", ClassUtils::capitalizeField))));

    }

    // Prosledjeni parovi statementArgs iz configuracije sa pravim className-ovima
    private Object[] prepareStatementResponse(List<StatementArg> statementArgs, List<String> classNames) {

        return IntStream.range(0, statementArgs.size()).mapToObj(i -> {
            StatementArg arg = statementArgs.get(i);
            String className = classNames.get(i);
            TypeWrapper type = getProperty(className, arg.getClassLayer(), null);

            if (arg.getArgumentType().equals("instance"))
                return Objects.nonNull(arg.getStringOperationFunc()) ?
                        arg.getStringOperationFunc().apply(type.getInstanceName()) : type.getInstanceName();

            return type.getTypeName();
        }).toArray(Object[]::new);
    }

    // Key su body za statemente
//    public Map<String, Object[]> getStatementArgs(String helperName, Map<Integer, List<String>> classNames) {
//        return methodStatementFactory.get(helperName)
//                .entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey, //body metode
//                        e -> prepareStatementResponse(e.getValue(), classNames)) // prepareRes -> dajem classNames sa
//                );
//    }

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
