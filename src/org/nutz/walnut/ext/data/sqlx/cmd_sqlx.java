package org.nutz.walnut.ext.data.sqlx;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.sqlx.loader.SqlHolder;
import org.nutz.walnut.ext.data.sqlx.loader.WnSqlHolder;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_sqlx extends JvmFilterExecutor<SqlxContext, SqlxFilter> {

    static Map<String, SqlHolder> sqlHolders = new HashMap<>();

    static SqlHolder getSqlHolder(WnIo io, WnObj oDir) {
        String key = oDir.id();
        SqlHolder sqls = sqlHolders.get(key);
        if (null == sqls) {
            synchronized (cmd_sqlx.class) {
                sqls = sqlHolders.get(key);
                if (null == sqls) {
                    sqls = new WnSqlHolder(io, oDir);
                    sqlHolders.put(key, sqls);
                }
            }
        }
        return sqls;
    }

    public cmd_sqlx() {
        super(SqlxContext.class, SqlxFilter.class);
    }

    @Override
    protected SqlxContext newContext() {
        return new SqlxContext();
    }

    @Override
    protected void prepare(WnSystem sys, SqlxContext fc) {
        WnObj oDir = Wn.checkObj(sys, "~/.sqlx");
        fc.sqls = getSqlHolder(sys.io, oDir);
    }

    @Override
    protected void output(WnSystem sys, SqlxContext fc) {
        // 子命令阻止输出
        if (fc.quiet) {
            return;
        }
    }

}
