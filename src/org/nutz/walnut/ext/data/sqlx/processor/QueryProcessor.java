package org.nutz.walnut.ext.data.sqlx.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wlog;

public class QueryProcessor implements SqlProcessor<List<NutBean>> {

    private static Log log = Wlog.getCMD();

    private NutBean toBean(ResultSet rs, ResultSetMetaData meta) throws SQLException {
        NutMap bean = new NutMap();
        int colCount = meta.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            String colName = meta.getColumnName(i);
            Object val = rs.getObject(i);
            bean.put(colName, val);
        }
        return bean;
    }

    @Override
    public List<NutBean> run(Connection conn, String sql) {
        List<NutBean> list = new LinkedList<>();
        if (log.isInfoEnabled()) {
            log.info(Wlog.msg(sql));
        }

        try {
            // 准
            Statement sta = conn.createStatement();

            // 执行
            ResultSet rs = sta.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();

            // 遍历结果集
            while (rs.next()) {
                NutBean bean = toBean(rs, meta);
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

            for (int i = 0; i < params.length; i++) {
                Object val = params[i];
                sta.setObject(i + 1, val);
            }

            // 执行
            ResultSet rs = sta.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            // 遍历结果集
            while (rs.next()) {
                NutBean bean = toBean(rs, meta);
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
