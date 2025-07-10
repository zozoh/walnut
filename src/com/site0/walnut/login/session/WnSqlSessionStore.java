package com.site0.walnut.login.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.SqlAtom;
import com.site0.walnut.ext.data.sqlx.util.SqlGetter;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.login.WnUserStore;
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnSessionStoreSetup;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class WnSqlSessionStore extends AbstractWnSessionStore {

    private static final Log log = Wlog.getAUTH();

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String sqlFetch;

    private String sqlDelete;

    private String sqlUpdate;

    private String sqlInsert;

    public WnSqlSessionStore(WnSessionStoreSetup setup) {
        this.defaultEnv = setup.defaultEnv;

        // SQL
        this.sqlFetch = setup.sqlFetch;
        this.sqlDelete = setup.sqlDelete;
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

    public WnSession getSession(String ticket, WnUserStore users) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlFetch);
        if (null == sqlt) {
            throw Er.create("session.fetch SQL without defined");
        }

        // 查询条件
        NutMap vars = Wlang.map("id", ticket);

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<WnSession>() {
            public WnSession doGet(Connection conn) throws SQLException {
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                rs.setFetchSize(2);
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnSimpleSession se = new WnSimpleSession();
                    // 设置标识
                    se.setTicket(bean.getString("ticket"));
                    se.setParentTicket(bean.getString("parent_ticket"));

                    // 过期时间
                    Date expiAt = Wtime.parseAnyDate(bean.get("expi_at"));
                    se.setExpiAt(expiAt.getTime());

                    // 设置环境变量
                    se.setEnv(bean.getAs("env", NutMap.class));

                    // 加载用户
                    String uid = bean.getString("u_id");
                    if (Ws.isBlank(uid)) {
                        log.warnf("session without u_id, ticket=%s", ticket);
                    }
                    // 读取用户
                    else {
                        WnLazyUser u = new WnLazyUser(users);
                        u.setInnerUser(users.getUserRace(),
                                       uid,
                                       bean.getString("u_name"),
                                       bean.getString("email"),
                                       bean.getString("phone"));
                        se.setUser(u);
                    }

                    // 返回会话
                    return se;
                }
                return null;
            }
        });
    }

    public void addSession(WnSession se) {
        // 获取用户
        WnUser u = se.getUser();

        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // 转换为一个 Bean
        NutMap bean = new NutMap();
        bean.put("id", se.getTicket());
        bean.put("expi_at", se.getExpiAtInUTC());
        bean.put("u_id", u.getId());
        bean.put("u_name", u.getName());
        bean.put("email", u.getEmail());
        bean.put("phone", u.getPhone());
        bean.put("env", se.getEnvAsStr());
        bean.put("ct", Wtime.formatUTC(now, fmt));
        bean.put("lm", Wtime.formatUTC(now, fmt));
        if (se.hasParentTicket()) {
            bean.put("parent_ticket", se.getParentTicket());
        }

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlInsert);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, bean));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToAdd", Json.toJson(bean));
        }
    }

    protected void _remove_session(WnSession se) {
        // 查询条件
        NutMap vars = Wlang.map("id", se.getTicket());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlDelete);

        // 保存到数据库里
        Sqlx.sqlRun(auth, new SqlAtom(log, sql, vars));
    }

    public void saveSessionEnv(WnSession se) {
        NutBean delta = new NutMap();
        delta.put("id", se.getTicket());
        delta.put("env", se.getEnvAsStr());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToSaveEnv", Json.toJson(delta));
        }
    }

    public void touchSession(WnSession se, long sessionDuration) {
        NutBean delta = new NutMap();
        se.setExpiAt(System.currentTimeMillis() + sessionDuration);

        delta.put("id", se.getTicket());
        delta.put("expi_at", se.getExpiAt());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToTouch", Json.toJson(delta));
        }
    }
}
