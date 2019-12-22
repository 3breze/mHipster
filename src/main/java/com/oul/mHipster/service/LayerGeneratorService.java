package com.oul.mHipster.service;

import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.Method;

import java.util.ArrayList;
import java.util.List;

public class LayerGeneratorService {

    private LayersConfig layersConfig;

    public LayerGeneratorService(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
    }

    public List<OldShitModel> generateLayers(Entity entity) {

        List<Layer> layers = layersConfig.getLayers();
        List<OldShitModel> oldShitModelList = new ArrayList<>();
        for (Layer layer : layers) {
            if (!layer.getName().contains("domain")) {
                String name = String.join("", entity.getClassName(), layer.getNamingSuffix());
                if (layer.getName().equals("service.impl")) {
                    for (Method method : layer.getMethods()) {
                        System.out.println(method.getType());
                        System.out.println(method.getMethodSignature());
                        System.out.println(method.getMethodBody());
//                        MethodSpec methodSpec = MethodSpec
//                                .methodBuilder("sta")
//                                .add
                    }
                }

            }
        }
        return oldShitModelList;
    }
}
