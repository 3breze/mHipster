package com.oul.mHipster.layerconfig.wrapper;

public class LayerInfoWrapper {
    private String namingSuffix;
    private String packageName;

    public LayerInfoWrapper() {
    }

    public LayerInfoWrapper(String namingSuffix, String packageName) {
        this.namingSuffix = namingSuffix;
        this.packageName = packageName;
    }

    public String getNamingSuffix() {
        return namingSuffix;
    }

    public void setNamingSuffix(String namingSuffix) {
        this.namingSuffix = namingSuffix;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
