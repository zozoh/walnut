package org.nutz.walnut.ext.data.sqlx;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.sqlx.loader.SqlHolder;
import org.nutz.walnut.ext.data.sqlx.processor.QueryProcessor;
import org.nutz.walnut.ext.sys.sql.WnDaoAuth;
import org.nutz.walnut.ext.sys.sql.WnDaos;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlog;

public class SqlxContext extends JvmFilterContext {

    private static Log log = Wlog.getCMD();

    public boolean quiet;

    public NutBean vars;

    public SqlHolder sqls;

    public WnDaoAuth auth;

    private Connection conn;

    public QueryProcessor query;

    public Object result;

    public SqlxContext() {
        this.vars = new NutMap();
        this.query = new QueryProcessor();
    }

    public void prepareToRun(WnSystem sys) {
        if (null == this.getConnection(sys)) {
            throw Er.create("e.cmd.sqlx.FailToGetConnection");
        }
    }

    public Connection getConnection(WnSystem sys) {
        if (null == conn) {
            if (null == auth) {
                throw Er.create("e.cmd.sqlx.conn.noAuth");
            }
            DataSource ds = WnDaos.getDataSource(auth);
            try {
                this.conn = ds.getConnection();
            }
            catch (SQLException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Fail get Connection!", e);
                }
            }
        }
        return this.conn;
    }

    public void closeConnection() {
        if (null != conn) {
            try {
                if (log.isTraceEnabled()) {
                    log.trace(Wlog.msg("conn.closed"));
                }
                conn.close();
            }
            catch (SQLException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Fail to Close!", e);
                }
            }
        }
    }
}
