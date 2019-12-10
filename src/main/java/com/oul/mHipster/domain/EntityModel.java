package com.oul.mHipster.domain;

import com.squareup.javapoet.TypeSpec;

public class EntityModel {

    private String className;
    private String instanceName;
    private String requestDtoClassName;
    private String requestDtoInstanceName;
    private String responseDtoClassName;
    private String responseDtoInstanceName;
    private TypeSpec typeSpec;
    private String layer;
    private String packageName;

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

    public String getRequestDtoClassName() {
        return requestDtoClassName;
    }

    public void setRequestDtoClassName(String requestDtoClassName) {
        this.requestDtoClassName = requestDtoClassName;
    }

    public String getRequestDtoInstanceName() {
        return requestDtoInstanceName;
    }

    public void setRequestDtoInstanceName(String requestDtoInstanceName) {
        this.requestDtoInstanceName = requestDtoInstanceName;
    }

    public String getResponseDtoClassName() {
        return responseDtoClassName;
    }

    public void setResponseDtoClassName(String responseDtoClassName) {
        this.responseDtoClassName = responseDtoClassName;
    }

    public String getResponseDtoInstanceName() {
        return responseDtoInstanceName;
    }

    public void setResponseDtoInstanceName(String responseDtoInstanceName) {
        this.responseDtoInstanceName = responseDtoInstanceName;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public static EntityModelBuilder builder() {
        return new EntityModelBuilder();
    }

    public static class EntityModelBuilder {
        private String className;
        private String instanceName;
        private String requestDtoClassName;
        private String requestDtoInstanceName;
        private String responseDtoClassName;
        private String responseDtoInstanceName;
        private TypeSpec typeSpec;
        private String layer;
        private String packageName;

        public EntityModelBuilder classAndInstanceName(String className) {
            this.className = className;
            this.instanceName = className.substring(0, 1).toUpperCase() + className.substring(1);
            return this;
        }

        public EntityModelBuilder requestClassAndInstanceName(String namingSuffix) {
            this.requestDtoClassName = String.join("", className, namingSuffix);
            this.requestDtoInstanceName = requestDtoClassName.substring(0, 1).toUpperCase() + requestDtoClassName.substring(1);
            return this;
        }

        public EntityModelBuilder responseClassAndInstanceName(String namingSuffix) {
            this.responseDtoClassName = String.join("", className, namingSuffix);
            this.responseDtoInstanceName = responseDtoClassName.substring(0, 1).toUpperCase() + responseDtoClassName.substring(1);
            return this;
        }

        public EntityModel build() {
            EntityModel entityModel = new EntityModel();
            entityModel.setClassName(className);
            entityModel.setInstanceName(instanceName);
            entityModel.setRequestDtoClassName(requestDtoClassName);
            entityModel.setRequestDtoInstanceName(requestDtoInstanceName);
            entityModel.setResponseDtoClassName(responseDtoClassName);
            entityModel.setResponseDtoInstanceName(responseDtoInstanceName);
            entityModel.setTypeSpec(typeSpec);
            entityModel.setLayer(layer);
            entityModel.setPackageName(packageName);
            return entityModel;
        }
    }
}
