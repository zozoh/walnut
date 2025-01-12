package com.site0.walnut.ext.data.sqlx.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;

public class SqlAtom {

    private Log log;

    private String sql;

    private Object[] params;

    public SqlAtom(Log log, WnSqlTmpl sqlt, NutBean vars) {
        this.log = log;
        List<SqlParam> cps = new ArrayList<>();
        this.sql = sqlt.render(vars, cps);
        this.params = WnSqls.getSqlParamsValue(cps);
    }

    public int exec(Connection conn) throws SQLException {
        if (log.isInfoEnabled()) {
            log.infof("SqlFieldGetter: sql=%s, params=%s", sql, Json.toJson(params));
        }
        PreparedStatement sta = conn.prepareStatement(sql);
        WnSqls.setParmas(sta, params);

        sta.execute();
        return sta.getUpdateCount();
    }
}
