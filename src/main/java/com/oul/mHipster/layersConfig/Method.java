package com.oul.mHipster.layersConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Method {

    private String type;
    @XmlElement
    private MethodSignature methodSignature;
    @XmlElement
    private String methodBody;

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    @XmlTransient
    public void setMethodSignature(MethodSignature methodSignature) {
        this.methodSignature = methodSignature;
    }

    public String getMethodBody() {
        return methodBody;
    }

    @XmlTransient
    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
    }
}

