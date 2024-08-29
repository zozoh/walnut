package com.site0.walnut.lookup.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.processor.QueryProcessor;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlElementMaker;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.lookup.bean.LookupConfig;
import com.site0.walnut.util.Wlog;

public class WnSqlLookup extends AbstractLookup {

    private static Log log = Wlog.getCMD();

    /**
     * 如果类型为 SQL，则需要一个数据库连接方式
     */
    private WnDaoAuth auth;

    /**
     * SQL 语句作为查询模板
     */
    private WnSqlTmpl queryTmpl;

    /**
     * SQL 语句作为单条记录获取模板
     */
    private WnSqlTmpl fetchTmpl;

    public WnSqlLookup(WnIo io, LookupConfig config) {
        super(config);
        this.prepareAuth(io, config.getDaoPath());
        this.prepareSqlTmpl(config.getSqlQuery(), config.getSqlFetch());
    }

    // public WnSqlLookup(WnDaoAuth auth, String query, String fetch) {
    // super(config);
    // this.auth = auth;
    // prepareSqlTmpl(query, fetch);
    // }

    private void prepareAuth(WnIo io, String daoPath) {
        WnObj oAuth = io.check(null, daoPath);
        String json = io.readText(oAuth);
        this.auth = Json.fromJson(WnDaoAuth.class, json);
    }

    private void prepareSqlTmpl(String query, String fetch) {
        WnSqlElementMaker eleMaker = new WnSqlElementMaker();
        this.queryTmpl = WnSqlTmpl.parse(query, eleMaker);
        this.fetchTmpl = WnSqlTmpl.parse(fetch, eleMaker);
    }

    private List<NutBean> __inner_query(NutBean context, WnSqlTmpl sqlT) {
        // 获取数据库连接
        DataSource ds = WnDaos.getDataSource(auth);
        Connection conn = null;
        try {
            conn = ds.getConnection();

            // 准备 SQL 语句

            List<SqlParam> cps = new ArrayList<>();
            String sql = sqlT.render(context, cps);
            Object[] sqlParams = WnSqls.getSqlParamsValue(cps);
            QueryProcessor q = new QueryProcessor();
            List<NutBean> re = q.runWithParams(conn, sql, sqlParams);
            return re;
        }
        catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail get Connection!", e);
            }
        }
        // 确保关闭
        finally {
            if (null != conn) {
                try {
                    conn.close();
                }
                catch (SQLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Fail Close Connection!", e);
                    }
                }
            }
        }

        return new ArrayList<>(1);
    }

    @Override
    public List<NutBean> lookup(String hint, int limit) {
        NutBean context = prepareHintForQuery(hint);
        if (context.isEmpty()) {
            return new ArrayList<>();
        }
        context.put("limit", limit);
        return __inner_query(context, this.queryTmpl);
    }

    @Override
    public List<NutBean> fetch(String id) {
        NutBean context = prepareHintForFetch(id);
        if (context.isEmpty()) {
            return new ArrayList<>();
        }
        return __inner_query(context, this.fetchTmpl);
    }

}
