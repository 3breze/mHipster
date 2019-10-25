package com.oul.mHipster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TestGeneratorService {
    public TypeSpec emit() {
        FieldSpec defaultName = FieldSpec
                .builder(String.class, "DEFAULT_NAME")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"Alice\"")
                .build();

        MethodSpec sumOfTen = MethodSpec
                .methodBuilder("sumOfTen")
                .addStatement("int sum = 0")
                .beginControlFlow("for (int i = 0; i <= 10; i++)")
                .addStatement("sum += i")
                .endControlFlow()
                .build();


        TypeSpec person = TypeSpec
                .classBuilder("Person")
                .addModifiers(Modifier.PUBLIC)
                .addField(defaultName)
                .addMethod(MethodSpec
                        .methodBuilder("getName")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("return this.name")
                        .build())
                .addMethod(MethodSpec
                        .methodBuilder("setName")
                        .addParameter(String.class, "name")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement("this.name = name")
                        .build())
                .addMethod(sumOfTen)
                .build();
        return person;
    }

    public List<JavaFile> layerItUp(TypeSpec entityClazz) {
        List<JavaFile> javaFileList = new ArrayList<>();
        JavaFile domainClazz = JavaFile
                .builder("com.oul.projectName.domain", entityClazz)
                .indent("    ")
                .build();
        javaFileList.add(domainClazz);
        JavaFile apiClazz = JavaFile
                .builder("com.oul.projectName.api", entityClazz)
                .indent("    ")
                .build();
        javaFileList.add(apiClazz);
        JavaFile serviceClazz = JavaFile
                .builder("com.oul.projectName.service", entityClazz)
                .indent("    ")
                .build();
        javaFileList.add(serviceClazz);
        JavaFile daoClazz = JavaFile
                .builder("com.oul.projectName.dao", entityClazz)
                .indent("    ")
                .build();
        javaFileList.add(daoClazz);
        return javaFileList;
    }
}
