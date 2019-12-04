package com.oul.mHipster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestGeneratorService {

    private Maconfig maconfig;
    private LayersConfig layersConfig;

    public TestGeneratorService(Maconfig maconfig, LayersConfig layersConfig) {
        this.maconfig = maconfig;
        this.layersConfig = layersConfig;
    }

    public void build() {
        for (Entity entity : maconfig.getEntities()) {
            TypeSpec typeSpec = buildEntity(entity);
            List<JavaFile> javaFileList = layerItUp(typeSpec);
            File myFile = new File("./src/main/java");
            for (JavaFile javaFile : javaFileList) {
//                try {
////                    javaFile.writeTo(myFile);
//                    javaFile.writeTo(System.out);
//                    System.out.println("------------------------");
//                    System.out.println();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

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

        List<Layer> layers = layersConfig.getLayers();
        List<JavaFile> javaFileList = new ArrayList<>();
        for (Layer layer : layers) {
            String packageName = String.join(".", maconfig.getGroupName(), maconfig.getArtifactName(), layer.getName());
            if (layer.getName().equals("domain")) {
                javaFileList.add(JavaFile
                        .builder(packageName, entityClazz)
                        .indent("    ")
                        .build());
            } else {
                String name = String.join("", entityClazz.name, layer.getNamingSuffix());
                if (layer.getName().equals("service.impl")) {
                    for (Method method : layer.getMethods()) {
                        System.out.println(method.getType());
                        System.out.println(method.getMethodSig());
                        System.out.println(method.getMethodBody());
                    }
                }
                TypeSpec typeSpec = TypeSpec.classBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .build();
                javaFileList.add(JavaFile
                        .builder(packageName, typeSpec)
                        .indent("    ")
                        .build());
            }
        }
        return javaFileList;
    }

    void serviceBuilder() {
        MethodSpec sumOfTen = MethodSpec
                .methodBuilder("HumanServiceImpl")
                .addStatement("find")
                .beginControlFlow("for (int i = 0; i <= 10; i++)")
                .addStatement("sum += i")
                .endControlFlow()
                .build();
    }
}
