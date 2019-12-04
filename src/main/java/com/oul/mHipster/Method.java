package com.oul.mHipster;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Method {

    private String type;
    @XmlElement
    private String methodSig;
    @XmlElement
    private String methodBody;

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getMethodSig() {
        return methodSig;
    }

    @XmlTransient
    public void setMethodSig(String methodSig) {
        this.methodSig = methodSig;
    }

    public String getMethodBody() {
        return methodBody;
    }

    @XmlTransient
    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
    }
}

