package com.site0.walnut.ext.data.sqlx.hdl;

import java.sql.Connection;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_dvcheck extends SqlxFilter {

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String sqlName = params.val_check(0);
        // version field name
        String key = params.val_check(1);
        // version $before:$after
        String val = params.val_check(2);
        NutMap vars = new NutMap(fc.getVarMap());

        String[] vv = Ws.splitIgnoreBlank(val, ":");
        vars.put("before", Wlang.map(key, vv[0]));
        vars.put("after", Wlang.map(key, vv[1]));

        // 准备 SQL 模板
        WnSqlTmpl sqlt = fc.sqls.get(sqlName);
        Connection conn = fc.getConnection();

        // 执行
        String sql = sqlt.render(vars, null);
        SqlExecResult re = fc.exec.run(conn, sql);

        // 判断
        if (re.updateCount != 1) {
            throw Er.create("e.cmd.sqlx_dvcheck.fail", re);
        }

    }

}
