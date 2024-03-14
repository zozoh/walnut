package com.site0.walnut.ext.data.sqlx.tmpl;

import java.util.Map;

import org.nutz.json.Json;

public class SqlParam {

    public SqlParam(Map.Entry<String, Object> en) {
        this.name = en.getKey();
        this.value = en.getValue();
    }

    public SqlParam(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private Object value;

    public String toString() {
        return String.format("%s=%s", name, Json.toJson(value));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
