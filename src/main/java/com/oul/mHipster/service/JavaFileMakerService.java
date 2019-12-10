package com.oul.mHipster.service;

import com.oul.mHipster.Util;
import com.oul.mHipster.domain.EntityModel;
import com.squareup.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFileMakerService {

    public void makeJavaFiles(List<EntityModel> entityModelList) {

        List<JavaFile> javaFileList = entityModelList.stream().map(entityModel -> JavaFile
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
