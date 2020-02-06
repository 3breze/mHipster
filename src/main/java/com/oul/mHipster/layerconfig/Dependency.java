package com.oul.mHipster.layerconfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Dependency {

    @XmlElement
    private String packageName;
    @XmlElement
    private String simpleName;

    public String getPackageName() {
        return packageName;
    }

    @XmlTransient
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    @XmlTransient
    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
}
