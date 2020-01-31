package com.oul.mHipster.layerconfig;

import javax.xml.bind.annotation.*;

@XmlRootElement
public class Parameter {
    private String type;

    private String name;

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @XmlValue
    public void setName(String name) {
        this.name = name;
    }
}
