package com.site0.walnut.ext.data.sqlx.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Nums;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.util.Wlog;

public class ExecProcessor implements SqlProcessor<SqlExecResult> {

    private static Log log = Wlog.getCMD();

    @Override
    public SqlExecResult run(Connection conn, String sql) {
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
        }
        // 准备返回
        SqlExecResult re = new SqlExecResult();

        try {
            // 准备语句
            Statement sta = conn.createStatement();

            // 执行
            sta.execute(sql);
            re.updateCount = sta.getUpdateCount();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }

        return re;
    }

    @Override
    public SqlExecResult runWithParams(Connection conn, String sql, Object[] params) {
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
            log.info(Wlog.msg("Params: " + Json.toJson(params)));
        }
        // 准备返回
        SqlExecResult re = new SqlExecResult();

        try {
            // 准备语句
            PreparedStatement sta = conn.prepareStatement(sql);
            WnSqls.setParmas(sta, params);

            // 执行
            sta.execute();
            re.updateCount = sta.getUpdateCount();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }

        return re;
    }

    @Override
    public SqlExecResult batchRun(Connection conn, String sql, List<Object[]> paramList) {
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
            int N = paramList.size();
            if (paramList.isEmpty()) {
                log.info(Wlog.msg("Params: []"));
            } else {
                log.info(Wlog.msgf("Params x%d:", N));
                int M = Math.min(3, N);
                for (int i = 0; i < M; i++) {
                    Object[] params = paramList.get(i);
                    log.info(Wlog.msgf(" %d) %s ", i, Json.toJson(params)));
                }
            }

        }
        // 准备返回
        SqlExecResult re = new SqlExecResult();

        try {
            // 准备语句
            PreparedStatement sta = conn.prepareStatement(sql);

            // sta.executeBatch();
            for (Object[] params : paramList) {
                WnSqls.setParmas(sta, params);
                sta.addBatch();
            }

            // 执行
            re.batchResult = sta.executeBatch();
            re.updateCount = sta.getUpdateCount();
            re.batchTotal = Nums.sum(re.batchResult);

        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }

        return re;
    }

}