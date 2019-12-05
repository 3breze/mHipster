package com.oul.mHipster.domainConfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LayersConfig {

    @XmlElementWrapper(name = "layers")
    @XmlElement(name = "layer")
    private List<Layer> layers = new ArrayList<>();

    public List<Layer> getLayers() {
        return layers;
    }

    @XmlTransient
    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }
}
