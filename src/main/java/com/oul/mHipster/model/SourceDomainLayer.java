package com.oul.mHipster.model;

import java.util.List;

public class SourceDomainLayer {

    private String rootPackageName;
    private List<Entity> entities;

    public SourceDomainLayer(String rootPackageName, List<Entity> entities) {
        this.rootPackageName = rootPackageName;
        this.entities = entities;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

}
