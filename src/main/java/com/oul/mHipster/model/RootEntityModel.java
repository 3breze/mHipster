package com.oul.mHipster.model;

import com.oul.mHipster.model.wrapper.TypeSpecWrapper;

import java.util.List;

public class RootEntityModel {

    private String rootPackageName;
    private List<Entity> entities;
    private List<TypeSpecWrapper> sharedClasses;

    public RootEntityModel(String rootPackageName, List<Entity> entities) {
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

    public List<TypeSpecWrapper> getSharedClasses() {
        return sharedClasses;
    }

    public void setSharedClasses(List<TypeSpecWrapper> sharedClasses) {
        this.sharedClasses = sharedClasses;
    }
}
