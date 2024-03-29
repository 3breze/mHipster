package com.oul.mHipster.layerconfig;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Layer {

    private String name;
    @XmlElement
    private String packageName;
    @XmlElement
    private String namingSuffix;
    @XmlElement
    private String type;
    @XmlElementWrapper(name = "methods")
    @XmlElement(name = "method")
    private List<Method> methods = new ArrayList<>();

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    @XmlTransient
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getNamingSuffix() {
        return namingSuffix;
    }

    @XmlTransient
    public void setNamingSuffix(String namingSuffix) {
        this.namingSuffix = namingSuffix;
    }

    public String getType() {
        return type;
    }

    @XmlTransient
    public void setType(String type) {
        this.type = type;
    }

    public List<Method> getMethods() {
        return methods;
    }

    @XmlTransient
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}
