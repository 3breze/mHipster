package com.oul.mHipster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestGeneratorService {

    private Maconfig maconfig;

    public TestGeneratorService(Maconfig maconfig) {
        this.maconfig = maconfig;
    }

    public void build(Maconfig maconfig) {
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


    public TypeSpec buildEntity(Entity entity) {

        TypeSpec person = TypeSpec
                .classBuilder(entity.getName())
                .addModifiers(Modifier.PUBLIC)
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
                .build();
        return person;
    }

    public List<JavaFile> layerItUp(TypeSpec entityClazz) {
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
