package com.oul.mHipster.model;

public class RelationAttribute extends Attribute {
    private String owner;
    private String relationType;

    public RelationAttribute(Class<?> type, String value, String owner, String relationType) {
        super(type, value);
        this.owner = owner;
        this.relationType = relationType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
}
