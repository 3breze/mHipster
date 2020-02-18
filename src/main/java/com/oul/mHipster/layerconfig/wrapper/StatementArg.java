package com.oul.mHipster.layerconfig.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class StatementArg {

    @XmlElement
    private String entityNameKey;
    @XmlElement
    private String classLayer;
    /*
    Required argument to be of type TypeName or String (instance name)
     */
    @XmlElement
    private Boolean isClazz;
    @XmlElement
    private String stringOperation;

    public StatementArg() {
    }

    public StatementArg(String entityNameKey, String classLayer, Boolean isClazz) {
        this.entityNameKey = entityNameKey;
        this.classLayer = classLayer;
        this.isClazz = isClazz;

    }

    public String getEntityNameKey() {
        return entityNameKey;
    }

    @XmlTransient
    public void setEntityNameKey(String entityNameKey) {
        this.entityNameKey = entityNameKey;
    }

    public String getClassLayer() {
        return classLayer;
    }

    @XmlTransient
    public void setClassLayer(String classLayer) {
        this.classLayer = classLayer;
    }

    public Boolean isClazz() {
        return isClazz;
    }

    @XmlTransient
    public void setIsClazz(Boolean isClazz) {
        this.isClazz = isClazz;
    }

    public String getStringOperation() {
        return stringOperation;
    }

    @XmlTransient
    public void setStringOperation(String stringOperation) {
        this.stringOperation = stringOperation;
    }

}
