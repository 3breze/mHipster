package com.oul.mHipster.model;

import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Map;

public class Entity {
    private String className;
    private String instanceName;
    private String packageName;
    private String optionalName;
    private Map<String, LayerClass> layers;
    private List<Attribute> attributes;
    private TypeSpec typeSpec;

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

    public Map<String, LayerClass> getLayers() {
        return layers;
    }

    public void setLayers(Map<String, LayerClass> layers) {
        this.layers = layers;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public TypeSpec getTypeSpec() {
        return typeSpec;
    }

    public void setTypeSpec(TypeSpec typeSpec) {
        this.typeSpec = typeSpec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String className;
        private String instanceName;
        private String packageName;
        private String optionalName;
        private Map<String, LayerClass> layers;
        private List<Attribute> attributes;
        private TypeSpec typeSpec;

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

        public Entity build() {
            Entity entity = new Entity();
            entity.setClassName(className);
            entity.setInstanceName(instanceName);
            entity.setPackageName(packageName);
            entity.setOptionalName(optionalName);
            entity.setLayers(layers);
            entity.setAttributes(attributes);
            entity.setTypeSpec(typeSpec);
            return entity;
        }
    }
}
