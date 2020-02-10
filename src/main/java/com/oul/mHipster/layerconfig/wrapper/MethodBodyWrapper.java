package com.oul.mHipster.layerconfig.wrapper;

import java.util.List;

public class MethodBodyWrapper {
    private String methodBody;
    private List<String> classNames;
    private List<String> types;

    public MethodBodyWrapper(String methodBody, List<String> types) {
        this.methodBody = methodBody;
        this.types = types;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }
}
