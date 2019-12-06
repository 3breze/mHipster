package com.oul.mHipster.domain;

import com.squareup.javapoet.TypeSpec;

public class TypeSpecWrapper {

    private TypeSpec typeSpec;
    private String layer;

    public TypeSpecWrapper(TypeSpec typeSpec, String layer) {
        this.typeSpec = typeSpec;
        this.layer = layer;
    }

    public TypeSpec getTypeSpec() {
        return typeSpec;
    }

    public void setTypeSpec(TypeSpec typeSpec) {
        this.typeSpec = typeSpec;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }
}
