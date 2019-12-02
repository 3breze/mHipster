package com.oul.mHipster;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Maconfig {

    private String type;
    private String groupName;
    private String artifactName;

    @XmlElementWrapper(name = "layers")
    @XmlElement(name = "layer")
    private List<Layer> layers;

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

    public String getGroupName() {
        return groupName;
    }

    @XmlValue
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    @XmlValue
    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    @XmlTransient
    public void setLayers(List<Layer> layers) {
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
