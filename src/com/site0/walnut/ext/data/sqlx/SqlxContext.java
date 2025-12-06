package com.site0.walnut.ext.data.sqlx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.mapl.Mapl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.hislog.SqlxHisConfig;
import com.site0.walnut.ext.data.sqlx.hislog.SqlxHisRuntime;
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

    private SqlxVarsMode varMode;

    /**
     * 存储一个整个命令周期都能有效的动态上下文
     * <p>
     * 建立这个对象的动机是： 我需要在一个 sqlx 周期内插入数据到两张表:
     * 
     * <pre>
     * sqlx 
     *  &#64; vars =I0 -as list 
     *  &#64; set code snowQ::5 -to list 
     *  &#64; exec pet.insert -p 
     *  &#64; vars =I1 -as list -reset 
     *  &#64; exec food.insert -p
     * </pre>
     * 
     * 我希望 food 的数据字段，有一个关联值 <code>pet_code=pet.code</code>
     * 由于这个值是我刚生成的，我希望在它新鲜热乎的时候，直接设置到上下文里，因此我希望这么写：
     * 
     * <pre>
     * sqlx 
     *  &#64; vars =I0 -as list 
     *  &#64; set code snowQ::5 -to list -savepipe 'pet.${id}'
     *  &#64; exec pet.insert -p 
     *  &#64; vars =I1 -as list -reset -put 'pet_code=pet.${id}'
     *  &#64; exec food.insert -p
     * </pre>
     * 
     */
    private NutMap pipeContext;

    public SqlHolder sqls;

    public WnDaoAuth auth;

    private Connection conn;

    public QueryProcessor query;

    public ExecProcessor exec;

    public SqlxHisRuntime hislog;

    public Object result;

    public SqlxContext() {
        this.query = new QueryProcessor(log);
        this.exec = new ExecProcessor(log);
        this.pipeContext = new NutMap();
    }

    public void loadAuth(WnSystem sys, String daoName) {
        this.auth = WnDaos.loadAuth(sys, daoName);
    }

    public void setup(WnSystem sys) {
        pipeContext.put("session", sys.session.toBean(sys.auth));
    }

    public NutMap getInput() {
        return input;
    }

    public void setInput(NutMap input) {
        this.input = input;
    }

    public NutMap getMergedInputAndPipeContext() {
        NutMap re = new NutMap();
        if (null != input) {
            re.putAll(input);
        }
        if (null != pipeContext) {
            re.putAll(pipeContext);
        }
        return re;
    }

    @SuppressWarnings("unchecked")
    public NutMap getInputOrPipeVarAsMap(String key) {
        NutMap re = null;
        if (null != input) {
            Map<String, Object> subMap = (Map<String, Object>) Mapl.cell(input, key);
            if (null != subMap) {
                re = NutMap.WRAP(subMap);
            }
        }
        if (null == re) {
            Map<String, Object> subMap = (Map<String, Object>) Mapl.cell(pipeContext, key);
            if (null != subMap) {
                re = NutMap.WRAP(subMap);
            }
        }
        return re;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<NutMap> getInputOrPipeVarAsList(String key) {
        Object v = null;
        if (null != input) {
            v = input.get(key);
        }
        if (null == v) {
            v = pipeContext.get(key);
        }
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
            Wn.explainMetaMacroInPlaceDeeply(varMap);
        }
        if (null != varList) {
            for (NutBean li : varList) {
                Wn.explainMetaMacroInPlaceDeeply(li);
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

    public boolean isVarsModeAsList() {
        return SqlxVarsMode.LIST == this.getVarsMode();
    }

    public boolean isVarsModeAsMap() {
        return SqlxVarsMode.MAP == this.getVarsMode();
    }

    public SqlxVarsMode getVarsMode() {
        // 自动判断
        if (null == this.varMode) {
            if (this.hasVarList()) {
                return SqlxVarsMode.LIST;
            }
            return SqlxVarsMode.MAP;
        }
        // 已经指定了
        return this.varMode;
    }

    public void setVarMode(SqlxVarsMode varMode) {
        this.varMode = varMode;
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

    public boolean hasHislogRuntime() {
        return null != this.hislog;
    }

    public void setHislogRuntime(WnSystem sys, String confPath) {
        WnObj oLogConf = Wn.checkObj(sys, confPath);
        String json = sys.io.readText(oLogConf);
        if (log.isDebugEnabled()) {
            log.debugf("sqlx hislog use id:%s, logConfPath=%s", oLogConf.id(), confPath);
        }
        SqlxHisConfig config = Json.fromJson(SqlxHisConfig.class, json);

        if (!config.hasValidLogs()) {
            if (log.isWarnEnabled()) {
                log.warnf("sqlx hislog without valid logs: id:%s, logConfPath=%s",
                          oLogConf.id(),
                          confPath);
            }
            return;
        }

        if (!config.hasValidTarget()) {
            if (log.isWarnEnabled()) {
                log.warnf("sqlx hislog without valid target: id:%s, logConfPath=%s",
                          oLogConf.id(),
                          confPath);
            }
            return;
        }

        this.hislog = new SqlxHisRuntime(sys, config, this);
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

    public void prepareToRun() {
        if (null == this.getConnection()) {
            throw Er.create("e.cmd.sqlx.FailToGetConnection");
        }
    }

    public NutMap getPipeContext() {
        return this.pipeContext;
    }

    public void putPipeContext(String keyPath, Object val) {
        Mapl.put(this.pipeContext, keyPath, val);
    }

    public void putAllPipeContext(NutBean vars) {
        this.pipeContext.putAll(vars);
    }

    public void appendToPipeContext(String key, Object val) {
        this.pipeContext.addv2(key, val);
    }

    public void closeConnectionFor(Connection c) {
        __close_connection(c);
    }

    public Connection getConnectionBy(WnDaoAuth auth, int transLevel, boolean autoCommit) {
        Connection re = null;
        try {
            re = __get_connection_by(auth, this.transLevel);
            re.setAutoCommit(autoCommit);
        }
        catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail get Connection!", e);
            }
        }
        return re;
    }

    private Connection __get_connection_by(WnDaoAuth auth, int transLevel) throws SQLException {
        if (null == auth) {
            throw Er.create("e.cmd.sqlx.conn.noAuth");
        }
        DataSource ds = WnDaos.getDataSource(auth);
        Connection re = ds.getConnection();
        if (this.hasTransLevel()) {
            if (transLevel > 0) {
                re.setTransactionIsolation(transLevel);
            }
        }
        return re;
    }

    public Connection getConnection() {
        if (null == conn) {
            try {
                conn = __get_connection_by(auth, this.transLevel);
                conn.setAutoCommit(false);
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
        __close_connection(this.conn);
    }

    private void __close_connection(Connection cn) {
        if (null == cn) {
            return;
        }
        try {
            if (log.isTraceEnabled()) {
                log.trace(Wlog.msg("conn.closed"));
            }
            if (!cn.getAutoCommit()) {
                cn.commit();
                cn.setAutoCommit(true);
            }
            cn.close();
        }
        catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to Close!", e);
            }
        }
    }
}
