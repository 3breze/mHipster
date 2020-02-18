package com.oul.mHipster.layerconfig.wrapper;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class HelperMethod {

    private String name;
    @XmlElementWrapper(name = "codeBlockStatements")
    @XmlElement(name = "codeBlockStatement")
    private List<CodeBlockStatement> codeBlockStatements = new ArrayList<>();

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public List<CodeBlockStatement> getCodeBlockStatements() {
        return codeBlockStatements;
    }

    @XmlTransient
    public void setCodeBlockStatements(List<CodeBlockStatement> codeBlockStatements) {
        this.codeBlockStatements = codeBlockStatements;
    }
}
