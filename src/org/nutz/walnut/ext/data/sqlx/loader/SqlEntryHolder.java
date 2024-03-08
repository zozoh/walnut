package org.nutz.walnut.ext.data.sqlx.loader;

import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public interface SqlEntryHolder {

    WnSqlTmpl get(String key);
    
}
