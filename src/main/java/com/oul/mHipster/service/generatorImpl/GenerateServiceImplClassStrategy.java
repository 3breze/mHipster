package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.PoetHelperService;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateServiceImplClassStrategy implements GenerateLayerStrategy {

    private PoetHelperService poetHelperService;
    private LayersConfig layersConfig;

    public GenerateServiceImplClassStrategy() {
        this.poetHelperService = new PoetHelperService();
        this.layersConfig = Util.getValue();
    }

    @Override
    public TypeSpec generate(Entity entity) {

        /**
         *  Mislio sam da iz atributa domenskih klasa izbacim relacije ka drugim entitetima, te da njih svedem na
         * prakticno flag (sa info o owneru i tipu relacije) kako bi ih koristio za ${builderInject} fillere u konfoguraciji
         */

        Map<String, LayerClass> layerMap = entity.getLayers();

        /**
         *  Izmesticu u poetHelper mada ne znam kako da ih izvlacim a da ne budu opet u nekoj mapi rezultati
         */
        TypeName domainClass = ClassName.get(entity.getPackageName(), entity.getClassName());
        TypeName requestDtoClass = ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
                layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName());
        TypeName responseDtoClass = ClassName.get(layerMap.get(LayerName.RESPONSE_DTO.toString()).getPackageName(),
                layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName());

        CodeBlock throwExceptionCodeBlock = poetHelperService.buildFindByIdCodeBlock(entity);


        /**
         * Iteriram kroz Method Body
         */
        layersConfig.getLayers().forEach(layer -> layer.getMethods().forEach(method -> {

        }));


//        FieldSpec daoField = FieldSpec
//                .builder(ClassName.get(layerMap.get(LayerName.REQUEST_DTO.toString()).getPackageName(),
//                        layerMap.get(LayerName.REQUEST_DTO.toString()).getClassName()),
//                        layerMap.get(LayerName.REQUEST_DTO.toString()).getInstanceName())
//                .addModifiers(Modifier.PRIVATE)
//                .build();
//
//        // CONSTRUCTOR
//        List<ParameterSpec> parameterSpecsList = entity.getAttributes().stream().map(entry -> ParameterSpec
//                .builder(ClassName.bestGuess(entry.getType().getName()), entry.getValue())
//                .build()).collect(Collectors.toList());
//
//        CodeBlock.Builder builder = CodeBlock.builder();
//        typeToValue.values().forEach(cb -> builder.addStatement("this.$N = $N", cb, cb));
//
//
//
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addAnnotation(Autowired.class)
//                .addModifiers(Modifier.PUBLIC)
//                .addParameters(parameterSpecsList)
//                .addCode(builder.build())
//                .build();
//
//
//        TypeSpec serviceClass = TypeSpec
//                .classBuilder("CustomerServiceImpl")
//                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Service.class)
//                .addField(customerDao)
//                .addMethod(constructor)
//                .addMethod(MethodSpec
//                        .methodBuilder("save")
//                        .addCode(throwExceptionCodeBlock)
//                        .addModifiers(Modifier.PUBLIC)
//                        .addAnnotation(Override.class)
//                        .addParameter(param)
//                        .addStatement(codeBlock2)
//                        .returns(responseDtoClass)
//                        .build())
//                .build();

        return null;
    }
}
