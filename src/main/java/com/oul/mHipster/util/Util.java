package com.oul.mHipster.util;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.layersConfig.wrapper.LayerInfoWrapper;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;

import java.util.HashMap;

public class Util {

    private static Util instance = new Util();
    private static final HashMap<LayerName, LayerInfoWrapper> layerInfoMap = new HashMap<>();
    private static LayersConfig config;

    public static void applyLayersConfig(LayersConfig layersConfig, MavenInfoWrapper mavenInfoWrapper) {
        config = layersConfig;
        layersConfig.getLayers().forEach(layer -> {
            String packageName = String.join(".", mavenInfoWrapper.getName(), layer.getName());
            layerInfoMap.put(LayerName.valueOf(layer.getName()), new LayerInfoWrapper(layer.getNamingSuffix(), packageName));
        });

    }

    public static Util getInstance() {
        return instance;
    }

    public static LayersConfig getValue() {
        return config;
    }

}
