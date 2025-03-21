package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class sqlx_exec extends SqlxFilter {

    private static Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain|noresult|batch)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        boolean batchMode = params.is("batch");
        WnMatch am = null;

        // 自动展开上下文
        if (params.is("explain")) {
            fc.explainVars();
        }

        // 判断是否执行
        if (params.has("test")) {
            Object test = params.get("test");
            am = AutoMatch.parse(test, false);
        }

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        if (log.isDebugEnabled()) {
            log.debugf("sqlx.exec", sqlt.toString());
        }
        Connection conn = fc.getConnection();

        SqlExecResult re;

        // 如果是批量
        if (fc.hasVarList()) {
            List<NutBean> beans = fc.getVarList();

            // 保护判断一下
            NutMap input = fc.getInput();
            if (null != am && !am.match(input)) {
                return;
            }

            // 批量模式
            if (batchMode) {
                NutBean context = beans.get(0);
                List<SqlParam> cps = new ArrayList<>();
                String sql = sqlt.render(context, cps);

                // 准备参数
                List<Object[]> paramList = Sqlx.getParams(beans, cps);
                re = fc.exec.batchRun(conn, sql, paramList);
            }
            // 单个模式
            else {
                re = new SqlExecResult();
                for (NutBean context : beans) {
                    List<SqlParam> cps = new ArrayList<>();
                    String sql = sqlt.render(context, cps);
                    Object[] sqlParams = Sqlx.getSqlParamsValue(cps);
                    SqlExecResult updateResult = fc.exec.runWithParams(conn, sql, sqlParams);
                    re.batchTotal += updateResult.batchTotal;
                    re.updateCount += updateResult.updateCount;
                }
            }

            // 准备更新日志
            if (null != fc.hislog) {
                fc.hislog.buildHislogForList(sqlName, beans);
            }
        }
        // 参数模式
        else if (fc.hasVarMap()) {
            NutBean record = fc.getVarMap();

            // 保护判断一下
            if (null != am && !am.match(record)) {
                return;
            }

            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(record, cps);
            Object[] sqlParams = Sqlx.getSqlParamsValue(cps);
            re = fc.exec.runWithParams(conn, sql, sqlParams);

            // 准备更新日志
            if (null != fc.hislog) {
                fc.hislog.buildHislog(sqlName, record);
            }
        }
        // 那么就是普通模式
        else {
            NutMap context = new NutMap();
            String sql = sqlt.render(context, null);
            re = fc.exec.run(conn, sql);
        }

        // 记录结果对象
        if (!params.is("noresult")) {
            // 合并
            if (fc.result instanceof SqlExecResult) {
                ((SqlExecResult) fc.result).mergeWith(re);
            }
            // 直接替换
            else {
                fc.result = re;
            }

            // 后续回查
            String fetch_by = params.get("fetch_by");
            NutMap fetch_vars = params.getMap("fetch_vars", new NutMap());
            String fetch_save = params.getString("fetch_save", "...");

            if (!Ws.isBlank(fetch_by)) {
                NutBean _vars = fetch_vars;
                // 用上下文作为变量集备份
                if (fc.hasVarMap()) {
                    _vars = fc.getVarMap();
                    if (null != fetch_vars && !fetch_vars.isEmpty()) {
                        _vars = (NutBean) Wn.explainObj(_vars, fetch_vars);
                    }
                }
                // 用列表的第一个作为上下文
                else if (fc.hasVarList()) {
                    _vars = fc.getVarList().get(0);
                    if (null != fetch_vars && !fetch_vars.isEmpty()) {
                        _vars = (NutBean) Wn.explainObj(_vars, fetch_vars);
                    }
                }

                List<SqlParam> cps = new ArrayList<>();
                WnSqlTmpl fetcht = fc.sqls.get(fetch_by);
                String fetch_sql = fetcht.render(_vars, cps);
                Object[] fetch_params = Sqlx.getSqlParamsValue(cps);
                re.list = fc.query.runWithParams(conn, fetch_sql, fetch_params);

                if (!Ws.isBlank(fetch_save) && re.list.size() > 0) {
                    NutBean reBean = re.list.get(0);
                    if (null != reBean) {
                        // 全部结构推入
                        if ("...".equals(fetch_save)) {
                            fc.putAllPipeContext(reBean);
                        }
                        // 简单推入
                        else {
                            fc.putPipeContext(fetch_save, reBean);
                        }
                    }
                }
            }
        }

    }

}
