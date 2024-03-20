package com.site0.walnut.ext.data.sqlx.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;

public class QueryProcessor implements SqlProcessor<List<NutBean>> {

    private static Log log = Wlog.getCMD();

    @Override
    public List<NutBean> run(Connection conn, String sql) {
        List<NutBean> list = new LinkedList<>();
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
        }

        try {
            // 准备语句
            Statement sta = conn.createStatement();

            // 执行
            ResultSet rs = sta.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();

            // 遍历结果集
            while (rs.next()) {
                NutBean bean = WnSqls.toBean(rs, meta);
                list.add(bean);
            }

            // 释放
            rs.close();
            sta.close();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }
        return list;
    }

    @Override
    public List<NutBean> runWithParams(Connection conn, String sql, Object[] params) {
        List<NutBean> list = new LinkedList<>();
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
            log.info(Wlog.msg("Params: " + Json.toJson(params)));
        }

        try {
            // 准备
            PreparedStatement sta = conn.prepareStatement(sql);
            WnSqls.setParmas(sta, params);

            // 执行
            ResultSet rs = sta.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            // 遍历结果集
            while (rs.next()) {
                NutBean bean = WnSqls.toBean(rs, meta);
                list.add(bean);
            }

            // 释放
            rs.close();
            sta.close();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("SQL Fail: %s", sql);
            }
            throw Er.wrap(e);
        }
        return list;
    }

    @Override
    public List<NutBean> batchRun(Connection conn, String sql, List<Object[]> params) {
        throw Wlang.noImplement();
    }

}
