package com.site0.walnut.ext.data.sqlx.loader;

import java.util.Map;

import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public interface SqlHolder {

    WnSqlTmpl get(String key);

    Map<String, SqlEntry> find(String keywords);

    void clear();

    int size();

}
