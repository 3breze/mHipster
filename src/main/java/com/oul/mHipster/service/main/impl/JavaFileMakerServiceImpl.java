package com.oul.mHipster.service.main.impl;

import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.service.main.JavaFileMakerService;
import com.oul.mHipster.service.poetic.impl.JPoetClassBuilderServiceImpl;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaFileMakerServiceImpl implements JavaFileMakerService {

    @Override
    public void makeJavaFiles(RootEntityModel rootEntityModel) {

        List<JavaFile> javaFileList = rootEntityModel.getEntities().stream()
                .flatMap(entity -> entity.getTypeSpecWrapperList().stream())
                .filter(typeSpecWrapper -> Objects.nonNull(typeSpecWrapper.getTypeSpec()))
                .map(typeSpecWrapper -> JavaFile
                        .builder(typeSpecWrapper.getPackageName(), typeSpecWrapper.getTypeSpec())
                        .indent("    ")
                        .build()).collect(Collectors.toList());

//        File myFile = new File("./src/main/java");
        javaFileList.forEach(javaFile -> {
            try {
//                javaFile.writeTo(myFile);
                System.out.println("------------------------");
                javaFile.writeTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            JavaFile.builder("paketi.paketi", new JPoetClassBuilderServiceImpl().buildResourceNotFoundException())
                    .indent("    ")
                    .build().writeTo(System.out);
            JavaFile.builder("paketi.paketi", new JPoetClassBuilderServiceImpl().buildValidationGroup())
                    .indent("    ")
                    .build().writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
