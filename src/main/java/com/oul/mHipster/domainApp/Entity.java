package com.oul.mHipster.domainApp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Entity {
    private String name;

    @XmlElement(name = "attribute")
    private List<Attribute> attributes = new ArrayList<>();

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    @XmlTransient
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

}
