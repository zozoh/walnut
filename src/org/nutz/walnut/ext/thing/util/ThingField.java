package org.nutz.walnut.ext.thing.util;

public class ThingField {

    private String key;

    private boolean required;

    private boolean virtual;

    private String type;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isObject() {
        return "object".equals(type);
    }

    public boolean isDateRange() {
        return "daterange".equals(type);
    }

    public boolean isDateTime() {
        return "datetime".equals(type);
    }

    public boolean isTime() {
        return "time".equals(type);
    }

    public boolean isInt() {
        return "int".equals(type);
    }

    public boolean isFloat() {
        return "float".equals(type);
    }

    public boolean isBoolean() {
        return "boolean".equals(type);
    }

}
