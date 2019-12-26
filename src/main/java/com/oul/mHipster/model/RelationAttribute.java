package com.oul.mHipster.model;

public class RelationAttribute extends Attribute {
    private String owner;
    private RelationType relationType;

    public RelationAttribute(Class<?> type, String value, String owner, RelationType relationType) {
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

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public String toString() {
        return "RelationAttribute{" +
                "type='" + super.getType() + '\'' +
                ",value='" + super.getValue() + '\'' +
                ",owner='" + owner + '\'' +
                ", relationType='" + relationType + '\'' +
                '}';
    }
}
