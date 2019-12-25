package com.oul.mHipster.model;

public class Attribute {
    private Class<?> type;
    private String value;

    public Attribute(Class<?> type, String value) {
        this.type = type;
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
