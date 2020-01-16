package com.oul.mHipster.model;

public class Attribute {
    private Class<?> type;
    private String fieldName;

    public Attribute(Class<?> type, String fieldName) {
        this.type = type;
        this.fieldName = fieldName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "type=" + type +
                ", value='" + fieldName + '\'' +
                '}';
    }
}
