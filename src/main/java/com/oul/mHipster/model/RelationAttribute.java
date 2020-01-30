package com.oul.mHipster.model;

public class RelationAttribute extends Attribute {
    private String typeArgument;
    private String owner;
    private RelationType relationType;

    public RelationAttribute(Class<?> type, String fieldName, String typeArgument, String owner, RelationType relationType) {
        super(type, fieldName);
        this.typeArgument = typeArgument;
        this.owner = owner;
        this.relationType = relationType;
    }

    public String getTypeArgument() {
        return typeArgument;
    }

    public void setTypeArgument(String typeArgument) {
        this.typeArgument = typeArgument;
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
                ",fieldName='" + super.getFieldName() + '\'' +
                ",typeArgument='" + typeArgument + '\'' +
                ",owner='" + owner + '\'' +
                ", relationType='" + relationType + '\'' +
                '}';
    }
}
