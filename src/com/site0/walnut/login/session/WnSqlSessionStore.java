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
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class WnSqlSessionStore extends AbstractWnSessionStore {

    private static final Log log = Wlog.getAUTH();

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String sqlQuery;

    private String sqlFetch;

    private String sqlDelete;

    private String sqlUpdate;

    private String sqlInsert;

    public WnSqlSessionStore(WnIo io, NutBean sessionVars, WnLoginSessionOptions setup) {
        super(io, sessionVars, setup.defaultEnv);

        // SQL
        this.sqlQuery = setup.sqlQuery;
        this.sqlFetch = setup.sqlFetch;
        this.sqlDelete = setup.sqlDelete;
        this.sqlUpdate = setup.sqlUpdate;
        this.sqlInsert = setup.sqlInsert;

        // 数据源
        String daoName = Ws.sBlank(setup.daoName, "default");
        this.auth = WnDaos.loadAuth(io, daoName, sessionVars);

        // 准备 SQL 管理器
        this.sqls = Sqlx.getSqlHolderByPath(io, sessionVars, setup.sqlHome);
    }

    @Override
    protected List<WnSession> _query(NutMap filter, NutMap sorter, int skip, int limit) {
        // 查询条件
        NutMap vars = new NutMap();
        if (null != filter && !filter.isEmpty()) {
            vars.put("filter", filter);
        }
        if (null != sorter && !sorter.isEmpty()) {
            vars.put("sorter", sorter);
        }
        if (skip > 0) {
            vars.put("skip", skip);
        }
        if (limit > 0) {
            vars.put("limit", limit);
        }

        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(sqlQuery);
        if (null == sqlt) {
            throw Er.create("session.query SQL without defined");
        }

        // 读取数据
        List<WnSession> reList = __load_session_list(vars, sqlt, limit);

        // 返回
        return reList;
    }

    private List<WnSession> __load_session_list(NutMap vars, WnSqlTmpl sqlt, int limit) {
        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<List<WnSession>>() {
            public List<WnSession> doGet(Connection conn) throws SQLException {
                List<WnSession> reList = new ArrayList<>(limit);
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                rs.setFetchSize(2);
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                int i = 0;
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnSimpleSession se = __bean_to_session(bean);
                    reList.add(se);
                    i++;
                    if (i >= limit) {
                        break;
                    }
                }
                return reList;
            }
        });
    }

    private WnSession __load_session(NutMap vars, String sqlName) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(sqlName);
        if (null == sqlt) {
            throw Er.create("session.fetch SQL without defined");
        }

        // 读取数据
        List<WnSession> list = __load_session_list(vars, sqlt, 1);

        // 返回
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public WnSession _get_one(String ticket) {
        // 查询条件
        NutMap vars = Wlang.map("id", ticket);

        // 执行读取
        return __load_session(vars, sqlFetch);
    }

    @Override
    public WnSession _find_one_by_uid_type(String uid, String type) {
        // 查询条件
        NutMap vars = Wlang.map("type", type);
        vars.put("u_id", uid);

        // 执行读取
        return __load_session(vars, sqlFetch);
    }

    @Override
    public WnSession _find_one_by_unm_type(String unm, String type) {
        // 查询条件
        NutMap vars = Wlang.map("type", type);
        vars.put("u_name", unm);

        // 执行读取
        return __load_session(vars, sqlFetch);
    }

    private static WnSimpleSession __bean_to_session(NutBean bean) {
        WnSimpleSession se = new WnSimpleSession();
        // 设置标识
        se.setSite(bean.getString("site"));
        se.setTicket(bean.getString("ticket"));
        se.setParentTicket(bean.getString("parent_ticket"));
        se.setChildTicket(bean.getString("child_ticket"));
        se.setDuration(bean.getInt("duration"));

        // 时间戳
        Date expiAt = Wtime.parseAnyDate(bean.get("expi_at"));
        se.setExpiAt(expiAt.getTime());
        Date ct = Wtime.parseAnyDate(bean.get("ct"));
        se.setExpiAt(ct.getTime());
        Date lm = Wtime.parseAnyDate(bean.get("lm"));
        se.setExpiAt(lm.getTime());

        // 设置用户
        String uid = bean.getString("u_id");
        if (Ws.isBlank(uid)) {
            log.warnf("session without u_id, ticket=%s", se.getTicket());
        }
        // 读取用户
        else {
            WnLazyUser u = new WnLazyUser();
            String name = bean.getString("u_name");
            String email = bean.getString("email");
            String phone = bean.getString("phone");
            u.setInnerUser(null, uid, name, email, phone);
            se.setUser(u);
        }

        // 设置环境变量
        se.setEnv(bean.getAs("env", NutMap.class));

        // 返回会话
        return se;
    }

    @Override
    public void addSession(WnSession se) {
        // 获取用户
        WnUser u = se.getUser();

        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        // 转换为一个 Bean
        NutMap bean = new NutMap();
        bean.put("id", se.getTicket());
        bean.put("type", se.getType());
        bean.put("expi_at", se.getExpiAtInUTC());
        bean.put("duration", se.getDuration());
        bean.put("duration", se.getDuration());
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
        if (se.hasChildTicket()) {
            bean.put("child_ticket", se.getChildTicket());
        }
        if (se.hasSite()) {
            bean.put("site", se.getSite());
        }

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlInsert);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, bean));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToAdd", Json.toJson(bean));
        }
    }

    @Override
    public void saveSessionEnv(WnSession se) {
        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";
        se.setExpiAt(now.getTime() + se.getDurationInMs());

        NutBean delta = new NutMap();
        delta.put("id", se.getTicket());
        delta.put("env", se.getEnvAsStr());
        delta.put("lm", Wtime.formatUTC(now, fmt));
        delta.put("expi_at", se.getExpiAtInUTC());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToSaveEnv", Json.toJson(delta));
        }
    }

    @Override
    public void saveSessionChildTicket(WnSession se) {
        if (!se.hasChildTicket()) {
            return;
        }
        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";
        se.setExpiAt(now.getTime() + se.getDurationInMs());

        NutBean delta = new NutMap();
        delta.put("id", se.getTicket());
        delta.put("child_ticket", se.getChildTicket());
        delta.put("lm", Wtime.formatUTC(now, fmt));
        delta.put("expi", se.getExpiAtInUTC());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToSaveSessionChildTicket", Json.toJson(delta));
        }

    }

    @Override
    public void touchSession(WnSession se, int duInSec) {
        Date now = new Date();
        String fmt = "yyyy-MM-dd HH:mm:ss";

        NutBean delta = new NutMap();
        delta.put("id", se.getTicket());
        se.setExpiAt(now.getTime() + duInSec * 1000L);
        delta.put("expi_at", se.getExpiAt());
        delta.put("lm", Wtime.formatUTC(now, fmt));

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, delta));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToTouch", Json.toJson(delta));
        }
    }

    @Override
    protected void _remove_session(WnSession se) {
        // 查询条件
        NutMap vars = Wlang.map("id", se.getTicket());

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlDelete);

        // 保存到数据库里
        Sqlx.sqlRun(auth, new SqlAtom(log, sql, vars));
    }
}
