package com.site0.walnut.ext.data.sqlx.tmpl;

import java.util.List;
import java.util.Map;

import org.nutz.json.Json;

import com.site0.walnut.util.Ws;

public class SqlParam {

    public static void setScopeTo(List<SqlParam> params, String scope) {
        if (null != params && params.size() > 0) {
            for (SqlParam p : params) {
                p.setScope(scope);
            }
        }
    }

    public SqlParam(Map.Entry<String, Object> en, String scope) {
        this.name = en.getKey();
        this.value = en.getValue();
        this.scope = scope;
    }

    public SqlParam(String name, Object value, String scope) {
        this.name = name;
        this.value = value;
        this.scope = scope;
    }

    private String scope;

    private String name;

    private Object value;

    public String toString() {
        if (!Ws.isBlank(scope)) {
            return String.format("[%s]:%s=%s", scope, name, Json.toJson(value));
        }
        return String.format("%s=%s", name, Json.toJson(value));
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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
