package com.oul.mHipster.domainConfig;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LayersConfig {

    private LombokSettings lombokSettings;

    @XmlElementWrapper(name = "layers")
    @XmlElement(name = "layer")
    private List<Layer> layers = new ArrayList<>();

    public LombokSettings getLombokSettings() {
        return lombokSettings;
    }

    @XmlTransient
    public void setLombokSettings(LombokSettings lombokSettings) {
        this.lombokSettings = lombokSettings;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    @XmlTransient
    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }
}
