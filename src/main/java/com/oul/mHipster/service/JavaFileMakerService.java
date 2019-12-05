package com.oul.mHipster.service;

import com.squareup.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavaFileMakerService {

    public void makeJavaFiles(List<JavaFile> javaFileList) {
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
