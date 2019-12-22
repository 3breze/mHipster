package com.oul.mHipster.layersConfig;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MethodSignature {

    private String returns;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<Parameter> parameters = new ArrayList<>();

    public String getReturns() {
        return returns;
    }

    @XmlAttribute
    public void setReturns(String returns) {
        this.returns = returns;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @XmlTransient
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}
