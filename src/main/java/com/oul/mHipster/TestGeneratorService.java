package com.oul.mHipster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestGeneratorService {

    private Maconfig maconfig;

    public TestGeneratorService(Maconfig maconfig) {
        this.maconfig = maconfig;
    }

    public void build() {
        for (Entity entity : maconfig.getEntities()) {
            TypeSpec typeSpec = buildEntity(entity);
            List<JavaFile> javaFileList = layerItUp(typeSpec);
            File myFile = new File("./src/main/java");
            for (JavaFile javaFile : javaFileList) {
                try {
//                    javaFile.writeTo(myFile);
                    javaFile.writeTo(System.out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public Class<T> getClassForType(String type){
//        switch (type){
//            case "String":
//                return String.class;
//                break;
//        }
//
//    }

    private TypeSpec buildEntity(Entity entity) {

        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (Attribute attribute : entity.getAttributes()) {
            fieldSpecList.add(FieldSpec
                    .builder(String.class, attribute.getValue())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
        }

        return TypeSpec
                .classBuilder(entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecList)
                .build();
    }

    private List<JavaFile> layerItUp(TypeSpec entityClazz) {
        List<Layer> layers = maconfig.getLayers();
        List<JavaFile> javaFileList = new ArrayList<>();
        for (Layer layer : layers) {
            String packageName = String.join(".", maconfig.getGroupName(), maconfig.getArtifactName(), layer.getName());
            javaFileList.add(JavaFile
                    .builder(packageName, entityClazz)
                    .indent("    ")
                    .build());
        }
        return javaFileList;
    }
}
