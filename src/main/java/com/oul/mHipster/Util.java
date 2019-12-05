package com.oul.mHipster;

import com.oul.mHipster.domainApp.EntityBuilderConfig;

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

    public static void applyEntityConfig(EntityBuilderConfig entityBuilderConfig){
        String packageName = String.join(".", entityBuilderConfig.getGroupName(), entityBuilderConfig.getArtifactName(), "domain");
    }

    public static void add(String[][] pairs) {
        for(String[] pair : pairs) {
            map.put(pair[0], pair[1]);
        }
    }

    public static void add(String[] keys, String[] values) {
        for (int i = 0; i < keys.length; ++i) {
            map.put(keys[i], values[i]);
        }
    }
}
