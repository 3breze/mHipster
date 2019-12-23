package com.oul.mHipster.util;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.layersConfig.wrapper.LayerInfoWrapper;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;

import java.util.HashMap;

public class Util {

    private static Util instance = new Util();
    private static final HashMap<LayerName, LayerInfoWrapper> layerInfoMap = new HashMap<>();

    public static void applyLayersConfig(LayersConfig layersConfig, MavenInfoWrapper mavenInfoWrapper) {
        layersConfig.getLayers().forEach(layer -> {
            String packageName = String.join(".", mavenInfoWrapper.getName(), layer.getName());
            layerInfoMap.put(LayerName.valueOf(layer.getName()), new LayerInfoWrapper(layer.getNamingSuffix(), packageName));
        });
    }

    public static Util getInstance() {
        return instance;
    }

    public static LayerInfoWrapper getValue(LayerName key) {
        return layerInfoMap.get(key);
    }

}
