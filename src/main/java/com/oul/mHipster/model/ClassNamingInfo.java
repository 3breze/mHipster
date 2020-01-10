package com.oul.mHipster.model;

public class ClassNamingInfo {
    private String className;
    private String instanceName;
    private String packageName;

    public ClassNamingInfo(String className, String instanceName, String packageName) {
        this.className = className;
        this.instanceName = instanceName;
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "LayerClass{" +
                "className='" + className + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
