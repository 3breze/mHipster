package com.oul.mHipster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGeneratorService {

    private Maconfig maconfig;

    private Map<String, String> layerNamingConv = new HashMap<String, String>() {{
        put("api", "Api");
        put("dao", "Dao");
        put("service", "Service");
        put("service.impl", "ServiceImpl");
        put("domain.dto.response", "ResponseDto");
        put("domain.dto.request", "RequestDto");
    }};

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
                    System.out.println("------------------------");
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        List<Layer> layers = maconfig.getLayers();
        List<JavaFile> javaFileList = new ArrayList<>();
        for (Layer layer : layers) {
            String packageName = String.join(".", maconfig.getGroupName(), maconfig.getArtifactName(), layer.getName());
            if (layer.getName().equals("domain")) {
                javaFileList.add(JavaFile
                        .builder(packageName, entityClazz)
                        .indent("    ")
                        .build());
            } else {
                String name = String.join("", entityClazz.name, layerNamingConv.get(layer.getName()));
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

    void serviceBuilder(){
        MethodSpec sumOfTen = MethodSpec
                .methodBuilder("HumanServiceImpl")
                .addStatement("find")
                .beginControlFlow("for (int i = 0; i <= 10; i++)")
                .addStatement("sum += i")
                .endControlFlow()
                .build();
    }
}
