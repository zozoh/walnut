package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_exec extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain|noresult)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);

        // 自动展开上下文
        if (params.is("explain")) {
            fc.explainVars();
        }

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection(sys);

        SqlExecResult re;

        // 如果是批量
        if (fc.hasVarList()) {
            List<NutBean> beans = fc.getVarList();

            NutBean context = beans.get(0);
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);

            // 准备参数
            List<Object[]> paramList = WnSqls.getParams(beans, cps);
            re = fc.exec.batchRun(conn, sql, paramList);

        }
        // 参数模式
        else if (fc.hasVarMap()) {
            NutBean context = fc.getVarMap();
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);
            Object[] sqlParams = WnSqls.getSqlParamsValue(cps);
            re = fc.exec.runWithParams(conn, sql, sqlParams);
        }
        // 那么就是普通模式
        else {
            NutMap context = new NutMap();
            String sql = sqlt.render(context, null);
            re = fc.exec.run(conn, sql);
        }

        // 记录结果对象
        if (!params.is("noresult")) {
            fc.result = re;

            // 后续回查
            String fetchSqlName = params.get("fetch_by");
            NutMap fetchVars = params.getMap("fetch_vars", new NutMap());

            if (!Ws.isBlank(fetchSqlName)) {
                // 用上下文作为变量集备份
                if (fc.hasVarMap()) {
                    fetchVars.attach(fc.getVarMap());
                }

                List<SqlParam> cps = new ArrayList<>();
                WnSqlTmpl fetcht = fc.sqls.get(fetchSqlName);
                String fetch_sql = fetcht.render(fetchVars, cps);
                Object[] fetch_params = WnSqls.getSqlParamsValue(cps);
                re.list = fc.query.runWithParams(conn, fetch_sql, fetch_params);
            }
        }

    }

}
