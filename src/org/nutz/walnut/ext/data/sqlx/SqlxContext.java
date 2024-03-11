package org.nutz.walnut.ext.data.sqlx;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.sqlx.loader.SqlHolder;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class SqlxContext extends JvmFilterContext {

    public boolean quiet;

    public NutBean vars;
    
    public SqlHolder sqls;

    public SqlxContext() {
        this.vars = new NutMap();
    }
}
