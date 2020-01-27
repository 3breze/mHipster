package com.oul.mHipster.model;

import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entity {
    private String className;
    private String instanceName;
    private String packageName;
    private String optionalName;
    private Map<String, ClassNamingInfo> layers;
    private List<Attribute> attributes;
    private List<TypeSpecWrapper> typeSpecWrapperList;

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

    public String getOptionalName() {
        return optionalName;
    }

    public void setOptionalName(String optionalName) {
        this.optionalName = optionalName;
    }

    public Map<String, ClassNamingInfo> getLayers() {
        return layers;
    }

    public void setLayers(Map<String, ClassNamingInfo> layers) {
        this.layers = layers;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<TypeSpecWrapper> getTypeSpecWrapperList() {
        return typeSpecWrapperList;
    }

    public void setTypeSpecWrapperList(List<TypeSpecWrapper> typeSpecWrapperList) {
        this.typeSpecWrapperList = typeSpecWrapperList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String className;
        private String instanceName;
        private String packageName;
        private String optionalName;
        private Map<String, ClassNamingInfo> layers;
        private List<Attribute> attributes;
        private List<TypeSpecWrapper> typeSpecWrapperList = new ArrayList<>();

        public Builder infoFields(Class<?> clazz) {
            String className = ClassUtils.getClassName(clazz);
            this.className = className;
            this.instanceName = ClassUtils.instanceNameBuilder(className);
            this.packageName = ClassUtils.getPackageName(clazz);
            this.optionalName = ClassUtils.optionalNameBuilder(className);
            return this;
        }

        public Builder attributes(List<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder typeSpecWrapperList(List<TypeSpecWrapper> typeSpecWrapperList) {
            this.typeSpecWrapperList = typeSpecWrapperList;
            return this;
        }

        public Entity build() {
            Entity entity = new Entity();
            entity.setClassName(className);
            entity.setInstanceName(instanceName);
            entity.setPackageName(packageName);
            entity.setOptionalName(optionalName);
            entity.setLayers(layers);
            entity.setAttributes(attributes);
            entity.setTypeSpecWrapperList(typeSpecWrapperList);
            return entity;
        }
    }
}
