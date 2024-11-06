package com.site0.walnut.ext.data.sqlx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.processor.ExecProcessor;
import com.site0.walnut.ext.data.sqlx.processor.QueryProcessor;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public class SqlxContext extends JvmFilterContext {

    private static Log log = Wlog.getCMD();

    public boolean quiet;

    /**
     * 事务级别
     * 
     * <ul>
     * <li><code>0</code> - 没有事务
     * <li><code>1</code> - TRANSACTION_READ_UNCOMMITTED
     * <li><code>2</code> - TRANSACTION_READ_COMMITTED
     * <li><code>4</code> - TRANSACTION_REPEATABLE_READ
     * <li><code>8</code> - TRANSACTION_SERIALIZABLE
     * <li><code>-1</code> - 数据库默认事务
     * </ul>
     */
    private int transLevel;

    private NutMap input;

    private NutMap varMap;

    private List<NutBean> varList;

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

    public NutMap getInput() {
        return input;
    }

    public void setInput(NutMap input) {
        this.input = input;
    }

    public NutMap getInputVarAsMap(String key) {
        return input.getAs(key, NutMap.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<NutMap> getInputVarAsList(String key) {
        Object v = input.get(key);
        if (v instanceof Map) {
            NutMap bean = NutMap.WRAP((Map) v);
            return Wlang.list(bean);
        }
        if (v instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) v;
            List<NutMap> list = new ArrayList<>(col.size());
            for (Object o : col) {
                NutMap bean = NutMap.WRAP((Map) o);
                list.add(bean);
            }
            return list;
        }
        return new LinkedList<>();
    }

    public boolean hasVarMap() {
        return null != this.varMap;
    }

    public void resetVarMap() {
        if (null != varMap) {
            this.varMap.clear();
        } else {
            this.varMap = new NutMap();
        }
    }

    public NutBean getVarMap() {
        return varMap;
    }

    public void explainVars() {
        if (null != varMap) {
            Wn.explainMetaMacro(varMap);
        }
        if (null != varList) {
            for (NutBean li : varList) {
                Wn.explainMetaMacro(li);
            }
        }
        // 如果返回的结果有内容，也尝试做一下 explain
        if (null != result && result instanceof SqlExecResult) {
            SqlExecResult re = (SqlExecResult) result;
            if (re.list != null && !re.list.isEmpty()) {
                NutBean bean = re.list.get(0);
                if (null != varMap) {
                    this.varMap = (NutMap) Wn.explainObj(bean, varMap);
                }
                if (null != varList) {
                    List<NutBean> list2 = new ArrayList<>(varList.size());
                    for (NutBean li : varList) {
                        NutMap li2 = (NutMap) Wn.explainObj(bean, li);
                        list2.add(li2);
                    }
                    this.varList = list2;
                }
            }
        }
    }

    public void assignVarMap(NutBean vars, String[] picks, String[] omits) {
        if (null == this.varMap) {
            this.varMap = new NutMap();
        }
        NutMap bean = __filter_bean(vars, picks, omits);
        if (null != bean) {
            this.varMap.putAll(bean);
        }
    }

    public void mergeVarMap(NutBean vars, String[] picks, String[] omits) {
        if (null == this.varMap) {
            this.varMap = new NutMap();
        }
        NutMap bean = __filter_bean(vars, picks, omits);
        if (null != bean) {
            this.varMap.mergeWith(bean);
        }
    }

    public boolean hasVarList() {
        return null != varList && !varList.isEmpty();
    }

    public void resetVarList() {
        if (null != varList) {
            varList.clear();
        } else {
            this.varList = new LinkedList<NutBean>();
        }
    }

    public List<NutBean> getVarList() {
        return varList;
    }

    private NutMap __filter_bean(NutBean bean, String[] picks, String[] omits) {
        if (null != picks && picks.length > 0) {
            bean = bean.pick(picks);
        }
        if (null != omits && omits.length > 0) {
            bean = bean.omit(omits);
        }
        return NutMap.WRAP(bean);
    }

    public void appendVarList(List<? extends NutBean> varList, String[] picks, String[] omits) {
        if (null == this.varList) {
            this.varList = new ArrayList<>(Math.max(20, varList.size()));
        }
        if ((null != picks && picks.length > 0) || (null != omits && omits.length > 0)) {
            List<NutMap> beans = new ArrayList<>(varList.size());
            for (NutBean bean : varList) {
                NutMap bean2 = __filter_bean(bean, picks, omits);
                beans.add(bean2);
            }
            this.varList.addAll(beans);
        } else {
            this.varList.addAll(varList);
        }
    }

    public boolean hasTransLevel() {
        return this.transLevel != 0;
    }

    public int getTransLevel() {
        return transLevel;
    }

    /**
     * 这里设置一下事务级别，如果已经在上下文里获取了连接，那么就要为连接设置上事务级别。
     * <p>
     * 否则这个标记会在连接第1次获取时生效。
     * 
     * @param transLevel
     *            事务级别
     * @see #getConnection(WnSystem)
     */
    public void setTransLevel(int transLevel) {
        this.transLevel = transLevel;
        if (null != this.conn && transLevel > 0) {
            try {
                this.conn.setTransactionIsolation(transLevel);
                this.conn.setAutoCommit(false);
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
    }

    public void rollback() throws SQLException {
        if (null != this.conn) {
            this.conn.rollback();
        }
    }

    public void prepareToRun(WnSystem sys) {
        if (null == this.getConnection(sys)) {
            throw Er.create("e.cmd.sqlx.FailToGetConnection");
        }
    }

    public NutBean prepareResultBean() {
        if (null != result && (result instanceof SqlExecResult)) {
            SqlExecResult re = (SqlExecResult) result;
            if (null != re.list && re.list.size() > 0) {
                return re.list.get(0);
            }
        }
        return new NutMap();
    }

    public Connection getConnection(WnSystem sys) {
        if (null == conn) {
            if (null == auth) {
                throw Er.create("e.cmd.sqlx.conn.noAuth");
            }
            DataSource ds = WnDaos.getDataSource(auth);
            try {
                this.conn = ds.getConnection();
                if (this.hasTransLevel()) {
                    if (this.transLevel > 0) {
                        this.conn.setTransactionIsolation(transLevel);
                    }
                    this.conn.setAutoCommit(false);
                }
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
                if (!conn.getAutoCommit()) {
                    conn.commit();
                    conn.setAutoCommit(true);
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
