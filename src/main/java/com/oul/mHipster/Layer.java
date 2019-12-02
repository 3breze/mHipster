package com.oul.mHipster;

import javax.xml.bind.annotation.XmlValue;

public class Layer {

    private String name;

    public String getName() {
        return name;
    }

    @XmlValue
    public void setName(String name) {
        this.name = name;
    }
}
