package com.site0.walnut.ext.data.sqlx.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.loader.WnSqlHolder;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
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

    public static <T> T sqlGet(WnDaoAuth auth, SqlGetter<T> callback) {
        DataSource ds = WnDaos.getDataSource(auth);
        Connection conn = null;
        PreparedStatement sta = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            return callback.doGet(conn);
        }
        catch (SQLException e) {
            throw Er.wrap(e);
        }
        finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != sta) {
                    sta.close();
                }
                if (null != conn) {
                    conn.close();
                }
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
    }

    public static int sqlRun(WnDaoAuth auth, SqlAtom callback) {
        DataSource ds = WnDaos.getDataSource(auth);
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            conn = ds.getConnection();
            return callback.exec(conn);
        }
        catch (SQLException e) {
            throw Er.wrap(e);
        }
        finally {
            try {
                if (null != sta) {
                    sta.close();
                }
                if (null != conn) {
                    conn.close();
                }
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
    }
}
