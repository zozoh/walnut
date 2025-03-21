package com.site0.walnut.impl.auth.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuths;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.SqlGetter;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class SqlAccountLoader extends AbstractAccountLoader {

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String sqlQuery;

    private String sqlFetch;

    private NutBean defaultMeta;

    public SqlAccountLoader(AccountLoaderOptions options) {
        WnIo io = options.io;
        NutBean sessionVars = options.sessionVars;
        this.defaultMeta = options.dftEnv;

        /**
         * 分析 SQL 设置，格式： ":{daoName}:{querySql}:{fetchSql?}"
         * 
         * <pre>
         * ::pet.select
         * ::pet.select:pet.fetch
         * :history:pet.select
         * :history:pet.select:pet.select:pet.fetch
         * </pre>
         */
        String[] ss = options.setup.split(":");

        // 数据源
        String daoName = Ws.sBlank(Ws.trim(ss[1]), "default");
        this.auth = WnDaos.loadAuth(io, daoName, sessionVars);
        this.sqlQuery = Ws.trim(ss[2]);
        if (ss.length > 2 && !Ws.isBlank(ss[3])) {
            this.sqlFetch = Ws.trim(ss[2]);
        }

        // 准备 SQL 管理器
        this.sqls = Sqlx.getSqlHolderByPath(io, sessionVars, "~/.sqlx");
    }

    @Override
    public List<WnAccount> queryAccount(WnQuery q) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlQuery);
        if (null == sqlt) {
            throw Er.create("select SQL without defined");
        }

        // 查询条件
        NutMap vars = q.toSqlVars(new Callback<NutMap>() {
            public void invoke(NutMap filter) {
                filter.remove("pid");
            }
        });

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<List<WnAccount>>() {
            public List<WnAccount> doGet(Connection conn) throws SQLException {
                List<WnAccount> list = new LinkedList<>();

                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnAccount u = new WnAccount(bean);
                    list.add(u);
                }

                return list;
            }
        });
    }

    @Override
    public WnAccount getAccount(WnAccount info) {
        // 用 ID 获取
        if (info.hasId()) {
            String uid = info.getId();
            return this.getAccountById(uid);
        }
        // 将信息转换为查询条件
        // 通常这个信息是手机号/邮箱/登录名等
        NutMap qmap = info.toBean(WnAuths.ABMM.QUERY_INFO);
        WnQuery q = new WnQuery();
        q.setAll(qmap);
        q.limit(2);

        List<WnAccount> list = queryAccount(q);
        return __get_one_account_from_list(list, "getAccount:" + info.toString());
    }

    private WnAccount __get_one_account_from_list(List<WnAccount> list, String hint) {
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw Er.create("e.auth.account.multiExists", hint);
        }
        return list.get(0);
    }

    @Override
    public WnAccount getAccountById(String uid) {
        // 指定了专有的 fetch SQL
        if (null != this.sqlFetch) {
            return __fetch_account_by_id(uid);
        }
        // 采用 query
        WnQuery q = Wn.Q.id(uid).limit(2);
        List<WnAccount> list = this.queryAccount(q);

        return __get_one_account_from_list(list, "getAccountById:" + uid);
    }

    private WnAccount __fetch_account_by_id(String uid) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlFetch);
        if (null == sqlt) {
            throw Er.create("select SQL without defined");
        }

        // 查询条件
        NutMap vars = Wlang.map("id", uid);

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<WnAccount>() {
            public WnAccount doGet(Connection conn) throws SQLException {
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    return new WnAccount(bean);
                }
                return null;
            }
        });
    }


}
