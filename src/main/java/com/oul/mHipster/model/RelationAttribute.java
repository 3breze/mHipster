package com.oul.mHipster.model;

public class RelationAttribute extends Attribute {
    private String classSimpleName;
    private String owner;
    private RelationType relationType;

    public RelationAttribute(Class<?> type, String fieldName, String classSimpleName, String owner, RelationType relationType) {
        super(type, fieldName);
        this.classSimpleName = classSimpleName;
        this.owner = owner;
        this.relationType = relationType;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public void setClassSimpleName(String classSimpleName) {
        this.classSimpleName = classSimpleName;
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
                ",classSimpleName='" + classSimpleName + '\'' +
                ",owner='" + owner + '\'' +
                ", relationType='" + relationType + '\'' +
                '}';
    }
}
