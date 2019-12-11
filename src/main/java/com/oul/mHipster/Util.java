package com.oul.mHipster;

import com.oul.mHipster.domain.LayerName;
import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;

import java.util.HashMap;

public class Util {
    private static final HashMap<LayerName, String> map = new HashMap<>();
    private static Util instance = new Util();

    private Util() {
    }

    public static Util getInstance() {
        return instance;
    }

    public static String getValue(LayerName key) {
        return map.get(key);
    }

    public static void applyLayersConfig(EntitiesConfig entitiesConfig, LayersConfig layersConfig) {
        for (Layer layer : layersConfig.getLayers()) {
            String packageName = String.join(".", entitiesConfig.getGroupName(), entitiesConfig.getArtifactName(), layer.getName());
            map.put(LayerName.valueOf(layer.getName()), packageName);
        }
    }
}
