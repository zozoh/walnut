package com.site0.walnut.ext.data.sqlx.util;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.loader.WnSqlHolder;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public abstract class Sqlx {

    private static Map<String, SqlHolder> sqlHolders = new HashMap<>();

    public static SqlHolder getSqlHolderByPath(WnSystem sys, String dirPath) {
        return getSqlHolderByPath(sys.io, sys.session.getVars(), dirPath);
    }

    public static SqlHolder getSqlHolderByPath(WnIo io, NutBean vars, String dirPath) {
        String path = Ws.sBlank(dirPath, "~/.sqlx");
        WnObj oDir = Wn.checkObj(io, vars, path);
        return getSqlHolder(io, oDir);
    }

    public static SqlHolder getSqlHolder(WnIo io, WnObj oDir) {
        String key = oDir.id();
        SqlHolder sqls = sqlHolders.get(key);
        if (null == sqls) {
            synchronized (Sqlx.class) {
                sqls = sqlHolders.get(key);
                if (null == sqls) {
                    sqls = new WnSqlHolder(io, oDir);
                    sqlHolders.put(key, sqls);
                }
            }
        }
        return sqls;
    }
}
