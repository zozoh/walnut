package org.nutz.walnut.ext.data.sqlx.loader;

import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public interface SqlHolder {

    WnSqlTmpl get(String key);

    void reset();

}
