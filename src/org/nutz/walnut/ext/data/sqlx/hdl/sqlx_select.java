package org.nutz.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.ext.data.sqlx.SqlxContext;
import org.nutz.walnut.ext.data.sqlx.SqlxFilter;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlParam;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sqlx_select extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "p");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        boolean useParam = params.is("p");

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection(sys);

        // 参数模式防止注入
        if (useParam) {
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(fc.vars, cps);
            Object[] sqlParams = WnSqls.getSqlParamsValue(cps);
            fc.result = fc.query.runWithParams(conn, sql, sqlParams);
        }
        // 普通模式
        else {
            String sql = sqlt.render(fc.vars, null);
            fc.result = fc.query.run(conn, sql);
        }
    }

}
