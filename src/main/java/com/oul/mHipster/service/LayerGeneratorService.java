package com.oul.mHipster.service;

import com.oul.mHipster.domain.EntityModel;
import com.oul.mHipster.domainApp.Entity;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;
import com.oul.mHipster.domainConfig.Method;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class LayerGeneratorService {

    private LayersConfig layersConfig;

    public LayerGeneratorService(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
    }

    public List<EntityModel> generateLayers(Entity entity) {

        List<Layer> layers = layersConfig.getLayers();
        List<EntityModel> entityModelList = new ArrayList<>();
        for (Layer layer : layers) {
            if (!layer.getName().contains("domain")) {
                String name = String.join("", entity.getName(), layer.getNamingSuffix());
                if (layer.getName().equals("service.impl")) {
                    for (Method method : layer.getMethods()) {
                        System.out.println(method.getType());
                        System.out.println(method.getMethodSig());
                        System.out.println(method.getMethodBody());
//                        MethodSpec methodSpec = MethodSpec
//                                .methodBuilder("sta")
//                                .add
                    }
                }

            }
        }
        return entityModelList;
    }
}
