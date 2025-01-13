package com.site0.walnut.ext.data.sqlx.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public class SqlGet<T> implements SqlGetter<T> {

    private Log log;

    private String sql;

    private Object[] params;

    private SqlResetSetGetter<T> getter;

    public SqlGet(Log log, WnSqlTmpl sqlt, NutBean vars, SqlResetSetGetter<T> getter) {
        this.log = log;
        List<SqlParam> cps = new ArrayList<>();
        this.sql = sqlt.render(vars, cps);
        this.params = Sqlx.getSqlParamsValue(cps);
        this.getter = getter;
    }

    @Override
    public T doGet(Connection conn) throws SQLException {
        if (log.isInfoEnabled()) {
            log.infof("SqlGet: sql=%s, params=%s", sql, Json.toJson(params));
        }
        PreparedStatement sta = conn.prepareStatement(sql);
        Sqlx.setParmas(sta, params);

        ResultSet rs = sta.executeQuery();
        if (rs.next()) {
            return this.getter.getValue(rs);
        }
        return null;
    }

}
