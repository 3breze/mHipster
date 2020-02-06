package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.Layer;
import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.poetic.JPoetHelperService;
import com.oul.mHipster.service.poetic.MethodBuilderService;
import com.oul.mHipster.service.poetic.impl.AttributeService;
import com.oul.mHipster.service.poetic.impl.JPoetHelperServiceImpl;
import com.oul.mHipster.service.poetic.impl.MethodServiceImpl;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;
import com.squareup.javapoet.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenerateApiClassStrategy implements GenerateLayerStrategy {

    private JPoetHelperService jPoetHelperService;
    private AttributeService attributeService;
    private MethodBuilderService methodBuilderService;

    public GenerateApiClassStrategy() {
        this.jPoetHelperService = new JPoetHelperServiceImpl();
        this.attributeService = new AttributeService();
        this.methodBuilderService = new MethodServiceImpl();
    }

    @Override
    public TypeSpecWrapper generate(Entity entity) {

        TypeWrapper serviceTypeNameWrapper = entityManagerService.getProperty(entity.getClassName(), "serviceClass");
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        fieldSpecList.add(FieldSpec
                .builder(serviceTypeNameWrapper.getTypeName(), serviceTypeNameWrapper.getInstanceName())
                .addModifiers(Modifier.PRIVATE)
                .build());

        MethodSpec constructor = jPoetHelperService.buildConstructor(fieldSpecList, "serviceClass");

        Optional<Layer> serviceImplLayerOptional = layersConfig.getLayers().stream()
                .filter(layer -> layer.getName().equals("API"))
                .findFirst();
        if (!serviceImplLayerOptional.isPresent()) throw new ConfigurationErrorException("Api layer not found.");

        List<MethodSpec> methods = serviceImplLayerOptional.get().getMethods().stream().map(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getType());

            CodeBlock methodBody = methodBuilderService.processMethodBody(entity, method.getMethodBody());

            List<ParameterSpec> parameters = methodBuilderService.getMethodParameters(entity, method, LayerName.API.name());

            String requestMethod = methodBuilderService.getRequestMethod(method.getType());

            TypeWrapper returnTypeName = attributeService.getTypeName(entity.getClassName(),
                    method.getMethodSignature().getReturns(), null);

            return methodBuilder
                    .addAnnotation(AnnotationSpec
                            .builder(RequestMapping.class)
                            .addMember("value", "$S", "/" + method.getType())
                            .addMember("method", "$T.$L", RequestMethod.class, requestMethod)
                            .addMember("produces", "$T.$L", MediaType.class, "APPLICATION_JSON_VALUE")
                            .build())
                    .addAnnotation(ResponseBody.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(parameters)
                    .addCode(methodBody)
                    .returns(returnTypeName.getTypeName())
                    .build();
        }).collect(Collectors.toList());

        ClassNamingInfo classNamingInfo = entity.getLayers().get(LayerName.API.toString());

        TypeSpec typeSpec = TypeSpec
                .classBuilder(classNamingInfo.getClassName())
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

        return new TypeSpecWrapper(typeSpec, classNamingInfo.getPackageName());
    }
}
