package com.site0.walnut.login.usr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.SqlAtom;
import com.site0.walnut.ext.data.sqlx.util.SqlGetter;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.Wuu;
import com.site0.walnut.val.id.WnSnowQMaker;

public class WnSqlUserStore extends AbstractWnUserStore {

    private static final WnSnowQMaker UidMaker = new WnSnowQMaker(null, 10);

    private static final Log log = Wlog.getAUTH();

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String sqlQuery;

    private String sqlFetch;

    private String sqlUpdate;

    private String sqlInsert;

    public WnSqlUserStore(WnUserStoreSetup setup) {
        this.defaultMeta = setup.defaultMeta;
        this.userRace = setup.userRace;

        // SQL
        this.sqlQuery = setup.sqlQuery;
        this.sqlFetch = setup.sqlFetch;
        this.sqlUpdate = setup.sqlUpdate;
        this.sqlInsert = setup.sqlInsert;

        // 数据源
        WnIo io = setup.io;
        NutBean sessionVars = setup.sessionVars;
        String daoName = Ws.sBlank(setup.daoName, "default");
        this.auth = WnDaos.loadAuth(io, daoName, sessionVars);

        // 准备 SQL 管理器
        this.sqls = Sqlx.getSqlHolderByPath(io, sessionVars, setup.sqlHome);
    }

    public WnUser addUser(WnUser u) {
        String uid = u.getId();
        if (Ws.isBlank(uid)) {
            u.setId(UidMaker.make(new Date(), null));
        }
        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";
        if (Ws.isBlank(uid)) {
            uid = UidMaker.make(now, null);
        }
        // 转换为一个 Bean
        NutMap bean = new NutMap();
        bean.put("id", u.getId());
        bean.put("nm", u.getName());
        bean.put("phone", u.getPhone());
        bean.put("email", u.getEmail());
        bean.put("main_group", u.getMainGroup());
        bean.put("roles", Ws.sBlank(Ws.join(u.getRoles(), ","), null));
        bean.put("last_login_at", u.getLastLoginAtInUTC());
        bean.put("salt", u.getSalt());
        bean.put("passwd", u.getPasswd());
        bean.put("ct", Wtime.formatUTC(now, fmt));
        bean.put("lm", Wtime.formatUTC(now, fmt));
        __join_user_meta_to_bean(u, bean);

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlInsert);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, bean));
        if (count <= 0) {
            throw Er.create("e.auth.user.FailToAdd", Json.toJson(bean));
        }

        // 返回
        return toWnUser(bean);
    }

    public void saveUserMeta(WnUser u) {
        if (!u.hasMeta()) {
            return;
        }
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        __join_user_meta_to_bean(u, delta);

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToSaveUserMeta", Json.toJson(delta));
        }
    }

    public void updateUserName(WnUser u) {
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        delta.put("nm", u.getName());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdateUserName", u.toString());
        }
    }

    public void updateUserPhone(WnUser u) {
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        delta.put("phone", u.getPhone());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdateUserPhone", Json.toJson(delta));
        }
    }

    public void updateUserEmail(WnUser u) {
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        delta.put("email", u.getEmail());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdateUserEmail", Json.toJson(delta));
        }
    }

    public void updateUserLastLoginAt(WnUser u) {
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        delta.put("last_login_at", u.getLastLoginAtInUTC());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdateUserLastLoginAt", Json.toJson(delta));
        }
    }

    public void updateUserPassword(WnUser u, String rawPassword) {
        String salt = Wuu.UU32();
        String passwd = Wn.genSaltPassword(rawPassword, salt);
        u.setSalt(salt);
        u.setPasswd(passwd);
        NutMap delta = new NutMap();
        delta.put("id", u.getId());
        delta.put("salt", salt);
        delta.put("passwd", passwd);

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdateUserPassword", Json.toJson(delta));
        }
    }

    @Override
    public List<WnUser> queryUser(WnQuery q) {
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

        return Sqlx.sqlGet(this.auth, new SqlGetter<List<WnUser>>() {
            public List<WnUser> doGet(Connection conn) throws SQLException {
                List<WnUser> list = new LinkedList<>();

                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnSimpleUser u = toWnUser(bean);
                    list.add(u);
                }

                return list;
            }

        });
    }

    @Override
    public WnUser getUserById(String uid) {
        // 指定了专有的 fetch SQL
        if (null != this.sqlFetch) {
            return __fetch_account_by_id(uid);
        }
        // 采用 query
        WnQuery q = Wn.Q.id(uid).limit(2);
        List<WnUser> list = this.queryUser(q);

        return __get_one_account_from_list(list, "getAccountById:" + uid);
    }

    private void __join_user_meta_to_bean(WnUser u, NutMap bean) {
        if (null != u.getMeta()) {
            for (Map.Entry<String, Object> en : u.getMeta().entrySet()) {
                String key = Ws.snakeCase(en.getKey());
                Object val = en.getValue();
                bean.put(key, val);
            }
        }
    }

    private WnUser __fetch_account_by_id(String uid) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlFetch);
        if (null == sqlt) {
            throw Er.create("user.fetch SQL without defined");
        }

        // 查询条件
        NutMap vars = Wlang.map("id", uid);

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<WnUser>() {
            public WnUser doGet(Connection conn) throws SQLException {
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    return toWnUser(bean);
                }
                return null;
            }
        });
    }

}
