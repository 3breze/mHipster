package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.PoetHelperService;
import com.oul.mHipster.service.impl.EntityManagerFactoryImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private PoetHelperService poetHelperService;
    private LayersConfig layersConfig;
    private EntityManagerFactory entityManagerFactory;

    public GenerateServiceImplClassStrategy() {
        this.poetHelperService = new PoetHelperService();
        this.entityManagerFactory = EntityManagerFactoryImpl.getInstance();
        this.layersConfig = Util.getValue();
    }

    /**
     * EntityManagerFactory je singleton da bi mogao da mu pristupas gde god je potrebno a cini mi se da bi to mofao da izbegnes.
     * Negde sam procitao da cim imas vise od jednog singletona u aplikaciji nesto si lose dizajnirao.
     * Cini mi se da bi trebala da postoji klasa EntityManager koja bi sadrzala entity i dodatne operacije nad njim koje
     * ovde radis u factory, i da bi samo prosledjivanje EntityManagera bilo dovoljno da se izgradi TypeSpec.
     *
     * EntityManagerFactory bi, kako mu ime kaze trebao jedino da sadrzi meta informacije, tj sve EntityManagere po konkretnim Class vrednostima.
     * Znaci nakon skeniranja da pokupi sve enititete i da im bude pristupna tacka gde ti trebaju
     */
    @Override
    public TypeSpec generate(Entity entity) {
//        CodeBlock throwExceptionCodeBlock = poetHelperService.buildFindByIdCodeBlock(entity);

        FieldTypeNameWrapper daoTypeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(), "daoClass");
        FieldSpec daoField = FieldSpec
                .builder(daoTypeNameWrapper.getTypeName(), daoTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec constructor = poetHelperService.buildConstructor(entity, "serviceImplClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream().filter(layer -> layer.getName().equals("SERVICE_IMPL")).findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Service layer not found.");

        List<MethodSpec> methods = new ArrayList<>();
        serviceImplLayerOptional.get().getMethods().forEach(method -> {
            List<ParameterSpec> parameters = new ArrayList<>();
            method.getMethodSignature().getParameters().forEach(parameter -> {
                FieldTypeNameWrapper typeNameWrapper = entityManagerFactory.getProperty(entity.getClassName(),
                        parameter.getType(), parameter.getName());

                parameters.add(
                        ParameterSpec
                                .builder(typeNameWrapper.getTypeName(), typeNameWrapper.getInstanceName())
                                .build());
            });

            TypeName returnTypeName = entityManagerFactory.getReturnTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns());

            methods.add(MethodSpec.methodBuilder(method.getType())
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .returns(returnTypeName)
                    .build());
        });

        return TypeSpec
                .classBuilder(entity.getLayers().get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(daoField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
