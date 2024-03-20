package com.site0.walnut.ext.data.sqlx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.processor.ExecProcessor;
import com.site0.walnut.ext.data.sqlx.processor.QueryProcessor;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public class SqlxContext extends JvmFilterContext {

    private static Log log = Wlog.getCMD();

    public boolean quiet;

    private NutMap varMap;

    private List<NutMap> varList;

    public SqlHolder sqls;

    public WnDaoAuth auth;

    private Connection conn;

    public QueryProcessor query;

    public ExecProcessor exec;

    public Object result;

    public SqlxContext() {
        this.query = new QueryProcessor();
        this.exec = new ExecProcessor();
    }

    public boolean hasVarMap() {
        return null != this.varMap;
    }

    public NutMap getVarMap() {
        return varMap;
    }
    
    public void prepareForUpdate() {
        if(null!=varMap) {
            Wn.explainMetaMacro(varMap);
        }
        if(null!=varList) {
            for(NutMap li :varList) {
                Wn.explainMetaMacro(li);
            }
        }
    }

    public void setVarMap(NutMap vars, String[] picks, String[] omits) {
        this.varMap = __filter_bean(vars, picks, omits);
    }

    public boolean hasVarList() {
        return null != varList && !varList.isEmpty();
    }

    public List<NutMap> getVarList() {
        return varList;
    }

    private NutMap __filter_bean(NutMap bean, String[] picks, String[] omits) {
        if (null != picks && picks.length > 0) {
            bean = bean.pick(picks);
        }
        if (null != omits && omits.length > 0) {
            bean = bean.omit(omits);
        }
        return bean;
    }

    public void setVarList(List<NutMap> varList, String[] picks, String[] omits) {
        if ((null != picks && picks.length > 0) || (null != omits && omits.length > 0)) {
            List<NutMap> beans = new ArrayList<>(varList.size());
            for (NutMap bean : varList) {
                NutMap bean2 = __filter_bean(bean, picks, omits);
                beans.add(bean2);
            }
            this.varList = beans;
        } else {
            this.varList = varList;
        }
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
