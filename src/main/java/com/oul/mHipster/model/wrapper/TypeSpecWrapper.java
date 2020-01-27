package com.oul.mHipster.model.wrapper;

import com.squareup.javapoet.TypeSpec;

public class TypeSpecWrapper {
    private TypeSpec typeSpec;
    private String packageName;

    public TypeSpecWrapper(TypeSpec typeSpec, String packageName) {
        this.typeSpec = typeSpec;
        this.packageName = packageName;
    }

    public TypeSpec getTypeSpec() {
        return typeSpec;
    }

    public void setTypeSpec(TypeSpec typeSpec) {
        this.typeSpec = typeSpec;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
