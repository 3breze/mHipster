package com.oul.mHipster.service;

import com.oul.mHipster.Util;
import com.oul.mHipster.domain.EntityModel;
import com.squareup.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaFileMakerService {

    public void makeJavaFiles(List<EntityModel> entityModelList) {

        List<JavaFile> javaFileList = new ArrayList<>();
        for (EntityModel entityModel : entityModelList) {
            String packageName = Util.getValue(entityModel.getLayer());
            javaFileList.add(JavaFile
                    .builder(packageName, entityModel.getTypeSpec())
                    .indent("    ")
                    .build());
        }

        File myFile = new File("./src/main/java");
        for (JavaFile javaFile : javaFileList) {
            try {
//                javaFile.writeTo(myFile);
                System.out.println("------------------------");
                javaFile.writeTo(System.out);
                System.out.println("------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
