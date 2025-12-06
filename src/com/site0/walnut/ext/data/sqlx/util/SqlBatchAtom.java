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

public class SqlBatchAtom implements SqlExecutor {
    private Log log;

    private WnSqlTmpl sqlt;

    private List<NutBean> varsList;

    public SqlBatchAtom(Log log, WnSqlTmpl sqlt, List<NutBean> varsList) {
        this.log = log;
        this.sqlt = sqlt;
        this.varsList = varsList;
    }

    public int exec(Connection conn) throws SQLException {
        int reCount = 0;

        for (NutBean vars : this.varsList) {
            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlt.render(vars, cps);
            Object[] params = Sqlx.getSqlParamsValue(cps);

            if (log.isInfoEnabled()) {
                log.infof("SqlAtom: sql=%s; params=%s", sql, Json.toJson(params));
            }
            PreparedStatement sta = conn.prepareStatement(sql);
            Sqlx.setParmas(sta, params);

            // 执行
            sta.execute();

            // 汇总结果
            reCount += sta.getUpdateCount();
        }
        return reCount;
    }
}
