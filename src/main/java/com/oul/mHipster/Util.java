package com.oul.mHipster;

import com.oul.mHipster.domain.LayerName;
import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.Layer;
import com.oul.mHipster.domainConfig.LayersConfig;

import java.util.HashMap;

public class Util {

        private static String entityBuilderConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/entitiesConfig.xml";
    private static String layersConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/layersConfig.xml";
//    private static String layersConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\layersConfig.xml";
//    private static String entityBuilderConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\entitiesConfig.xml";

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

    public static String getLayersConfig() {
        return layersConfig;
    }

    public static void setLayersConfig(String layersConfig) {
        Util.layersConfig = layersConfig;
    }

    public static String getEntityBuilderConfig() {
        return entityBuilderConfig;
    }

    public static void setEntityBuilderConfig(String entityBuilderConfig) {
        Util.entityBuilderConfig = entityBuilderConfig;
    }
}
