package com.oul.mHipster;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TestPoetApi {
    public static void main(String[] args) {

        // MODEL
        Map<String, String> typeToValue = new HashMap<>();
        typeToValue.put("String", "name");
        typeToValue.put("Long", "id");

        TypeName domainClass = ClassName.get("com.whatever.domain", "Customer");

        TypeName requestDtoClass = ClassName.get("com.whatever.domain.request", "CustomerRequestDto");

        TypeName responseDtoClass = ClassName.get("com.whatever.domain.response", "CustomerResponseDto");

        TypeName resourceNotFoundClass = ClassName.get("com.whatever.exception", "ResourceNotFoundException");

        // findById po dao u zavisnosti od m2o i m2m
        // methodSig je nepotreban?
        Map<String, String> map = new HashMap<>();
        map.put("domainClazz", "Consumer");
        map.put("domain", "consumer");
        map.put("responseClazz", "ConsumerResponseDto");
        map.put("optional", "optionalCustomer");
        map.put("dao", "customerDao");

        // FIND BY ID CODE BLOCK - za sopstveni entity na findById,update,delete; za m2m i m2o
        CodeBlock throwException = CodeBlock.builder()
                .addStatement("Optional<$T> $L = $L.findById(id)", domainClass, map.get("optional"), map.get("dao"))
                .beginControlFlow("if ($L.isEmpty())", map.get("optional"))
                .addStatement("throw new $T(\"$T $L\")", resourceNotFoundClass, resourceNotFoundClass, "not found!")
                .endControlFlow()
                .addStatement("$T $L = $L.get()", domainClass, map.get("domain"), map.get("optional"))
                .build();


        // LOMBOK BUILDER
        StringBuffer builderStingBuffer = new StringBuffer();
        typeToValue.values().forEach(e -> builderStingBuffer.append(".").append(e).append("(customerRequestDto.get")
                .append(e.substring(0, 1).toUpperCase()).append(e.substring(1)).append("())"));
        CodeBlock lombokBuilder = CodeBlock.builder()
                .addStatement("$T $L = $T.builder()$L.build()", domainClass, "customer", domainClass, builderStingBuffer.toString())
                .build();


        map.put("builderInject", "xox");
        map.put("findByIdInject", "xxx");


//        String methodBody = "Page&lt;${domainClazz}&gt; page = ${dao}.findAll(predicate, pageable);\n" +
//                "                        return new PageImpl&lt;&gt;(\n" +
//                "                        page.stream().map(${responseClazz}::new).collect(Collectors.toList()), pageable,\n" +
//                "                        page.getTotalElements())";

        String methodBody = "${findByIdInject}\n" +
                "${builderInject}\n" +
                "${dao}.save(${domain})\n" +
                "return new ${responseClazz}(${domain})";

        String regex = "\\$\\{(.*?)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(methodBody);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, map.get(matcher.group(1)));
        }
        matcher.appendTail(sb);

        String codeBlock = sb.toString();

        String codeBlock1 = codeBlock.replaceAll("&lt;", "<");
        String codeBlock2 = codeBlock1.replaceAll("&gt;", ">");

        // Dao za klasu i m2m i m2o domain service
        List<ParameterSpec> parameterSpecsList = typeToValue.entrySet().stream().map(entry -> ParameterSpec
                .builder(ClassName.bestGuess(entry.getKey()), entry.getValue())
                .build()).collect(Collectors.toList());

        // u zavisnosti od m2m i m2o veza
        // treba da autowire service drugih entita a ne dao
        FieldSpec customerDao = FieldSpec
                .builder(ClassName.get("com.whatever.dao", "CustomerDao"), "customerDao")
                .addModifiers(Modifier.PRIVATE)
                .build();


        ParameterSpec param = ParameterSpec
                .builder(requestDtoClass,
                        "customerRequestDto")
                .build();

        CodeBlock.Builder builder = CodeBlock.builder();
        typeToValue.values().forEach(cb -> builder.addStatement("this.$N = $N", cb, cb));

        // Jos jedna ex
//        MethodSpec.Builder payloadInterpreterMethod = MethodSpec.methodBuilder("payloadInterpreter")
//                .addModifiers(Modifier.PRIVATE)
//                .addModifiers(Modifier.STATIC);
//        if (condition) {
//            payloadInterpreterMethod.addParameter(Response.class, "getResponse");
//        } else {
//            payloadInterpreterMethod.addParameter(CoapResponse.class, "getResponse");
//        }

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addAnnotation(Autowired.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecsList)
                .addCode(builder.build())
                .build();


        TypeSpec serviceClass = TypeSpec
                .classBuilder("CustomerServiceImpl")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(customerDao)
                .addMethod(constructor)
                .addMethod(MethodSpec
                        .methodBuilder("save")
                        .addCode(throwException)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(param)
                        .addStatement(codeBlock2)
                        .returns(ParameterizedTypeName.get(ClassName.bestGuess("Pageeelone"),
                                responseDtoClass))
//                        .returns(responseDtoClass)
                        .build())
                .build();

        JavaFile javaFile = JavaFile
                .builder("lol.kek", serviceClass)
                .indent("    ")
                .build();

        String ttt = "Page<responseClazz>";
        System.out.println(ttt.substring(0, ttt.indexOf("<")));
        System.out.println(ttt.substring(ttt.indexOf("<") + 1, ttt.indexOf(">")));

        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
