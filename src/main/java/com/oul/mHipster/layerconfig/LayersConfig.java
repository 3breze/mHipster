package com.oul.mHipster.layerconfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LayersConfig {

    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dependency")
    private List<Dependency> dependencies = new ArrayList<>();

    @XmlElementWrapper(name = "layers")
    @XmlElement(name = "layer")
    private List<Layer> layers = new ArrayList<>();

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @XmlTransient
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    @XmlTransient
    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }
}
