package com.site0.walnut.ext.data.sqlx.hislog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.id.WnSnowQMaker;

public class SqlxHisRuntime {

    private static final Log log = Wlog.getCMD();

    private static final String BATCH_NO_KEY = "__batch_no";

    private static final ValueMaker BatchNoMaker = new WnSnowQMaker(null, 6);

    SqlxHisConfig config;

    private WnSystem sys;

    private SqlxContext fc;

    private WnExplain globalAssign;

    private List<HisRuntimeItem> logs;

    private String batchNo;

    public SqlxHisRuntime(WnSystem sys, SqlxHisConfig config, SqlxContext fc) {
        this.sys = sys;
        this.config = config;
        this.fc = fc;
        this.batchNo = BatchNoMaker.make(new Date(), null).toString();
        this.prepare();
    }

    private void prepare() {
        if (null != config.getAssign()) {
            globalAssign = WnExplains.parse(config.getAssign());
        }
        if (null != config.getLogs()) {
            this.logs = new ArrayList<>(config.getLogs().length);
            for (HisConfigItem confItem : config.getLogs()) {
                if (!confItem.isValid()) {
                    if (log.isWarnEnabled()) {
                        log.warnf("hislog item is invalid: %s", Json.toJson(confItem));
                    }
                    continue;
                }
                HisRuntimeItem it = new HisRuntimeItem(sys, confItem);
                this.logs.add(it);
            }
        }
    }

    public void buildHislogForList(Date now, String sqlName, List<NutBean> records) {
        NutBean myContext = createGlobalContext();
        for (NutBean record : records) {
            NutMap theContext = new NutMap();
            theContext.putAll(myContext);
            __build_hislog(now, sqlName, theContext, record);
        }
    }

    public void buildHislogForRecord(Date now, String sqlName, NutBean record) {
        NutBean myContext = createGlobalContext();
        __build_hislog(now, sqlName, myContext, record);
    }

    private void __build_hislog(Date now, String sqlName, NutBean myContext, NutBean record) {
        for (HisRuntimeItem rtItem : logs) {
            if (!rtItem.isMatchRecord(record, myContext) || !rtItem.hasToPipeKey()) {
                continue;
            }
            if (rtItem.trySqlName(sqlName, myContext)) {
                myContext.put("item", record);
                NutBean hisMeta = rtItem.createLogRecord(now, myContext, record);
                String pipeKey = rtItem.getToPipeKey();
                fc.appendToPipeContext(pipeKey, hisMeta);
                break;
            }
        }
    }

    public void insertToTarget() {
        if (!config.hasValidTarget()) {
            return;
        }
        NutMap pipe = fc.getPipeContext();
        for (SqlxHisTarget ta : config.getTarget()) {
            // 确保设置是正确的
            if (!ta.isValid()) {
                if (log.isWarnEnabled()) {
                    log.warnf("sqlx hislog invalid target: %s", Json.toJson(ta));
                }
                continue;
            }

            // 获取历史记录列表
            List<NutMap> beans = pipe.getAsList(ta.getFrom(), NutMap.class);
            if (null == beans || beans.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debugf("sqlx hislog empty-target from=%s", ta.getFrom());
                }
                continue;
            }

            // 执行插入
            __insert_to_target(ta, beans);

        }
    }

    private void __insert_to_target(SqlxHisTarget ta, List<NutMap> beans) {
        // 准备 SQL 模板
        WnSqlTmpl sqlt = fc.sqls.get(ta.getSqlInsert());

        boolean useSelftDao = !Ws.isBlank(ta.getDao());

        // 获取连接
        Connection conn = null;
        try {
            if (useSelftDao) {
                WnDaoAuth auth = WnDaos.loadAuth(sys, ta.getDao());
                conn = fc.getConnectionBy(auth, 1, false);
            } else {
                conn = fc.getConnection();
            }
            // 默认采用批量模式
            NutBean first = beans.get(0);
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(first, cps);

            // 准备参数
            List<Object[]> paramList = Sqlx.getParams(beans, cps);
            SqlExecResult re = fc.exec.batchRun(conn, sql, paramList);

            if (log.isInfoEnabled()) {
                log.infof("sqlx hislog, beans.size=%d, re.batchTotal=%d, re.updateCount=%d",
                          beans.size(),
                          re.batchTotal,
                          re.updateCount);
            }
        }
        catch (Throwable e) {
            if (useSelftDao) {
                try {
                    conn.rollback();
                }
                catch (SQLException e1) {
                    log.errorf("!!Rollback Fail for dao=%s: %s", ta.getDao(), e1.toString());
                }
            }
            throw Er.wrap(e);
        }
        finally {
            if (useSelftDao) {
                fc.closeConnectionFor(conn);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public NutBean createGlobalContext() {
        NutBean g = fc.getMergedInputAndPipeContext();
        if (null != globalAssign) {
            Object re = globalAssign.explain(g);
            NutMap ctx = NutMap.WRAP((Map<String, Object>) re);
            ctx.put(BATCH_NO_KEY, this.batchNo);
            ctx.put("domain", sys.getMyGroup());
            return ctx;
        }
        return new NutMap();
    }

}
