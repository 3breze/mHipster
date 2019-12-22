package com.oul.mHipster.util;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.layersConfig.wrapper.LayerInfoWrapper;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;

import java.util.HashMap;

public class Util {

    private static Util instance = new Util();
    private static final HashMap<LayerName, LayerInfoWrapper> layerInfoMap = new HashMap<>();
    private static String layersConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/layersConfig.xml";
    //    private static String layersConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\layersConfig.xml";

    public static void applyLayersConfig(LayersConfig layersConfig, MavenInfoWrapper mavenInfoWrapper) {
        layersConfig.getLayers().forEach(layer -> {
            String packageName = String.join(".", mavenInfoWrapper.getName(), layer.getName());
            layerInfoMap.put(LayerName.valueOf(layer.getName()), new LayerInfoWrapper(layer.getNamingSuffix(), packageName));
        });
    }

    public static String instanceNameBuilder(String className) {
        return className.substring(0, 1).toUpperCase() + className.substring(1);
    }

    public static String optionalNameBuilder(String className) {
        return "optional" + className;
    }

    public static Util getInstance() {
        return instance;
    }

    public static LayerInfoWrapper getValue(LayerName key) {
        return layerInfoMap.get(key);
    }

    public static String getLayersConfig() {
        return layersConfig;
    }

    public static void setLayersConfig(String layersConfig) {
        Util.layersConfig = layersConfig;
    }

}
