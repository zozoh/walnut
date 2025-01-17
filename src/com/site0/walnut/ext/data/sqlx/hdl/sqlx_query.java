package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class sqlx_query extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "p");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        boolean useParam = params.is("p");

        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection();

        NutBean context = fc.getVarMap();
        // 参数模式防止注入
        if (useParam) {
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(context, cps);
            Object[] sqlParams = Sqlx.getSqlParamsValue(cps);
            fc.result = fc.query.runWithParams(conn, sql, sqlParams);
        }
        // 普通模式
        else {
            String sql = sqlt.render(context, null);
            fc.result = fc.query.run(conn, sql);
        }
    }

}
