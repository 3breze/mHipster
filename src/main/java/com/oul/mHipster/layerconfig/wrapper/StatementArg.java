package com.oul.mHipster.layerconfig.wrapper;

import java.util.function.Function;

public class StatementArg {

    private String classLayer;
    /*
    Required argument to be of type TypeName or String (instance name)
     */
    private String argumentType;
    private Function<String, String> stringOperationFunc;

    @SafeVarargs
    public StatementArg(String classLayer, String argumentType, Function<String, String>... stringOperations) {
        this.classLayer = classLayer;
        this.argumentType = argumentType;
        this.stringOperationFunc = stringOperations.length != 0 ? stringOperations[0] : null;
    }

    public String getClassLayer() {
        return classLayer;
    }

    public void setClassLayer(String classLayer) {
        this.classLayer = classLayer;
    }

    public String getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(String argumentType) {
        this.argumentType = argumentType;
    }

    public Function<String, String> getStringOperationFunc() {
        return stringOperationFunc;
    }

    public void setStringOperationFunc(Function<String, String> stringOperationFunc) {
        this.stringOperationFunc = stringOperationFunc;
    }
}
