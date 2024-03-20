package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class sqlx_exec extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "p");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection(sys);

        // 如果是批量
        if (fc.hasVarList()) {
            List<NutMap> beans = fc.getVarList();

            NutMap context = beans.get(0);
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);

            // 准备参数
            List<Object[]> paramList = WnSqls.getParams(beans, cps);
            fc.exec.batchRun(conn, sql, paramList);

        }
        // 参数模式
        else if (fc.hasVarMap()) {
            NutMap context = fc.getVarMap();
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);
            Object[] sqlParams = WnSqls.getSqlParamsValue(cps);
            fc.result = fc.exec.runWithParams(conn, sql, sqlParams);
        }
        // 那么就是普通模式
        else {
            NutMap context = new NutMap();
            String sql = sqlt.render(context, null);
            fc.result = fc.exec.run(conn, sql);
        }

    }

}
