package com.site0.walnut.ext.data.sqlx.loader;

import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public interface SqlHolder {

    WnSqlTmpl get(String key);

    void reset();

}
