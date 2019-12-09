package com.oul.mHipster.domainApp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EntitiesConfig {

    private String type;
    @XmlElement
    private String groupName;
    @XmlElement
    private String artifactName;
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

    @XmlTransient
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    @XmlTransient
    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    @XmlTransient
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

}
