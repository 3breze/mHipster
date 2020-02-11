package com.oul.mHipster.layerconfig.wrapper;

import java.util.List;
import java.util.function.Consumer;

public class StatementArg {

    private String classInfo;
    private List<String> types;
    private boolean instance;

    @SafeVarargs
    public StatementArg(String classInfo, Consumer<String>... capitalize) {
        this.classInfo = classInfo;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
