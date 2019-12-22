package com.oul.mHipster.layersConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LombokSettings {

    private String active;

    public String getActive() {
        return active;
    }

    @XmlAttribute
    public void setActive(String active) {
        this.active = active;
    }
}
