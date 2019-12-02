package com.oul.mHipster;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Maconfig {

    private String type;
    private String layers;

    @XmlElementWrapper(name = "entities")
    @XmlElement(name = "entity")
    private List<Entity> entities = new ArrayList<>();

    public String getType() {
        return type;
    }

    @XmlAttribute
    public void setType(String type) {
        this.type = type;
    }

    public String getLayers() {
        return layers;
    }

    public void setLayers(String layers) {
        this.layers = layers;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    @XmlTransient
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

}
