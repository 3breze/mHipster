package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.*;
import com.oul.mHipster.service.helper.JPoetHelperService;
import com.oul.mHipster.service.helper.MethodBuilderService;
import com.oul.mHipster.service.helper.impl.AttributeBuilderService;
import com.oul.mHipster.service.helper.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.helper.impl.MethodBuilderServiceImpl;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateApiClassStrategy implements GenerateLayerStrategy {

    private LayersConfig layersConfig;
    private EntityManagerService entityManagerService;
    private JPoetHelperService jPoetHelperService;
    private AttributeBuilderService attributeBuilderService;
    private MethodBuilderService methodBuilderService;

    public GenerateApiClassStrategy() {
        /**
         * Nisam pametan da li bi layersConfig i entityManager trebali da dolaze kao dependency u svakoj od strategija,
         * cak i ako ti nisu potrebni u svakoj od njih (hleba ne jedu a mozda zatrebaju naknadno :))
         * https://enterprisecraftsmanship.com/posts/singleton-vs-dependency-injection/
         * Pokusao sam da googlam i da vidim sta kazu, u principu singleton ima smisla ako ti je nesto rasuto svuda po projektu
         * i nije povezano jasnim poslovnim procesima, u svakom drugom slucaju trebalo bi forsirati dependency injection.
         * Ja bih verovatno izvukao AbsractLayerStrategy koja bi primala menadzera i lazer config i instancirala ostale
         * stvari koje su svima potrebne (JPoetHelperService), ali nmg da tvrdim da bi to bolji pristup od ovog, mozda malo
         * cistiji ali bi zakomplikovao izgradnju strategija u strategy factory...
         */
        this.layersConfig = Util.getValue();
        this.entityManagerService = EntityManagerFactory.getInstance();
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeBuilderService = new AttributeBuilderService();
        this.methodBuilderService = new MethodBuilderServiceImpl();
    }

    @Override
    public TypeSpec generate(Entity entity) {

        FieldTypeNameWrapper serviceTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "serviceClass");
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        fieldSpecList.add(FieldSpec
                .builder(serviceTypeNameWrapper.getTypeName(), serviceTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build());

        MethodSpec constructor = jPoetHelperService.buildConstructor(entity, fieldSpecList, "serviceClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("API"))
                .findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Api layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());

            CodeBlock methodBody = methodBuilderService.processMethodBody(entity, method.getMethodBody());

            List<ParameterSpec> parameters = methodBuilderService.getMethodParameters(entity, method, LayerName.API.name());

            String requestMethod = methodBuilderService.getRequestMethod(method.getType());

            TypeName returnTypeName = attributeBuilderService.getReturnTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns());

            return methodBuilder
                    .addAnnotation(AnnotationSpec
                            .builder(RequestMapping.class)
                            .addMember("value", "$S", "/" + method.getType())
                            .addMember("method", "$T.$L", RequestMethod.class, requestMethod)
                            .addMember("produces", "$T.$L", MediaType.class, "APPLICATION_JSON_VALUE")
                            .build())
                    .addAnnotation(RequestBody.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .addCode(methodBody)
                    .returns(returnTypeName)
                    .build();
        }).collect(Collectors.toList());

        return TypeSpec
                .classBuilder(entity.getLayers().get(LayerName.SERVICE_IMPL.toString()).getClassName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec
                        .builder(RequestMapping.class)
                        .addMember("value", "$S", "/" + entity.getInstanceName())
                        .build())
                .addFields(fieldSpecList)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }
}
