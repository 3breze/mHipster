package com.oul.mHipster;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) {

        // MODEL
        Map<String, String> typeToValue = new HashMap<>();
        typeToValue.put("String", "name");
        typeToValue.put("Long", "id");

        TypeName domainClass = ClassName.get("com.whatever.domain", "Customer");

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


        CodeBlock throwException = CodeBlock.builder()
                .addStatement("Optional<$T> $L = $L.findById(id);",domainClass,map.get("optional"), map.get("dao"))
                .beginControlFlow("if ($L.isEmpty())", map.get("optional"))
                .addStatement("throw new $T($S$T)", resourceNotFoundClass, "not found!", resourceNotFoundClass)
                .endControlFlow()
                .build();
        String throwExceptionInject = throwException.toString();


        StringBuffer sb1 = new StringBuffer();
        typeToValue.values().forEach(e -> sb1.append(".").append(e).append("(customerRequestDto.get")
                .append(e.substring(0, 1).toUpperCase()).append(e.substring(1)).append(").build();"));
        CodeBlock lombokBuilder = CodeBlock.builder()
                .addStatement("$T.builder()$L", domainClass, sb1.toString())
                .build();
        String lombokInject = lombokBuilder.toString();

        map.put("builderInject", lombokInject);
        map.put("findByIdInject", throwExceptionInject);


        String regex = "\\$\\{(.*?)}";

//        String methodBody = "Page&lt;${domainClazz}&gt; page = ${dao}.findAll(predicate, pageable);\n" +
//                "                        return new PageImpl&lt;&gt;(\n" +
//                "                        page.stream().map(${responseClazz}::new).collect(Collectors.toList()), pageable,\n" +
//                "                        page.getTotalElements())";

        String methodBody = "Optional&lt;${domainClazz}&gt; ${optional} = ${dao}.findById(id);\n" +
                "                        if (${optional}.isEmpty()) {\n" +
                "                        throw new ResourceNotFoundException(\"${domainClazz} not found!\");\n" +
                "                        }\n" +
                "                        ${domainClazz} ${domain} = ${optional}.get();\n" +
                "                        ${builderInject}\n" +
                "                        ${dao}.save(${domain})\n" +
                "                        return new ${responseClazz}(${domain})";

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


        List<ParameterSpec> parameterSpecsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : typeToValue.entrySet()) {
            parameterSpecsList.add(ParameterSpec
                    .builder(ClassName.bestGuess(entry.getKey()), entry.getValue())
                    .build());


            // u zavisnosti od m2m i m2o veza
            // treba da autowire service drugih entita a ne dao
            FieldSpec customerDao = FieldSpec
                    .builder(ClassName.get("com.whatever.dao", "CustomerDao"), "customerDao")
                    .addModifiers(Modifier.PRIVATE)
                    .addAnnotation(Autowired.class)
                    .build();

            TypeName requestDtoClass = ClassName.get("com.whatever.domain.request", "CustomerRequestDto");
            ParameterSpec param = ParameterSpec
                    .builder(requestDtoClass,
                            "customerRequestDto")
                    .build();


            MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addParameters(parameterSpecsList).build();


            TypeSpec serviceClass = TypeSpec
                    .classBuilder("CustomerServiceImpl")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Service.class)
                    .addField(customerDao)
                    .addMethod(constructor)
                    .addMethod(MethodSpec
                            .methodBuilder("save")
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(param)
                            .addStatement(codeBlock2)
                            .returns(responseDtoClass)
                            .build())
                    .build();

            JavaFile javaFile = JavaFile
                    .builder("lol.kek", serviceClass)
                    .indent("    ")
                    .build();


            try {
                javaFile.writeTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
