package com.oul.mHipster.model.wrapper;

import com.squareup.javapoet.TypeName;

public class FieldTypeNameWrapper {
    private TypeName typeName;
    private String instanceName;

    public FieldTypeNameWrapper(TypeName typeName, String instanceName) {
        this.typeName = typeName;
        this.instanceName = instanceName;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
