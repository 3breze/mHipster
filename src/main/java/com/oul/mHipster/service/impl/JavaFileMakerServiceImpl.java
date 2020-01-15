package com.oul.mHipster.service.impl;

import com.oul.mHipster.model.SourceDomainLayer;
import com.oul.mHipster.service.JavaFileMakerService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFileMakerServiceImpl implements JavaFileMakerService {

    @Override
    public void makeJavaFiles(SourceDomainLayer sourceDomainLayer) {

        List<JavaFile> javaFileList = sourceDomainLayer.getEntities().stream()
                .filter(entity -> entity.getTypeSpec() != null)
                .map(entityModel -> JavaFile
                        .builder(entityModel.getPackageName(), entityModel.getTypeSpec())
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
    }
}
