package com.oul.mHipster.service;

import com.oul.mHipster.todelete.OldShitModel;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFileMakerService {

    public void makeJavaFiles(List<OldShitModel> oldShitModelList) {

        List<JavaFile> javaFileList = oldShitModelList.stream().map(entityModel -> JavaFile
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
