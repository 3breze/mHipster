package com.oul.mHipster;

import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;

import java.util.HashMap;

public class Util {
    private static final HashMap<String, String> map = new HashMap<>();
    private static Util instance = new Util();

    private Util() {
    }

    public static Util getInstance() {
        return instance;
    }

    public static String getValue(String key) {
        return map.get(key);
    }

    public static void applyLayersConfig(EntitiesConfig entitiesConfig, LayersConfig layersConfig) {
        for (Layer layer : layersConfig.getLayers()) {
            String packageName = String.join(".", entitiesConfig.getGroupName(), entitiesConfig.getArtifactName(), layer.getName());
            map.put(layer.getName(), packageName);
        }
    }
}
