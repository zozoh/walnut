package com.site0.walnut.login.role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
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
import com.site0.walnut.ext.data.sqlx.util.SqlBatchAtom;
import com.site0.walnut.ext.data.sqlx.util.SqlGetter;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.login.WnLoginRoleOptions;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.id.WnSnowQMaker;

public class WnSqlRoleStore extends AbstractWnRoleStore {

    private static final Log log = Wlog.getAUTH();
    private static final WnSnowQMaker IdMaker = new WnSnowQMaker(null, 10);

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String sqlQuery;

    private String sqlFetch;

    private String sqlDelete;

    private String sqlInsert;

    private String sqlUpdate;

    public WnSqlRoleStore(WnIo io, NutBean sessionVars, WnLoginRoleOptions options) {
        super(io, sessionVars);
        // SQL
        this.sqlQuery = options.sqlQuery;
        this.sqlFetch = options.sqlFetch;
        this.sqlDelete = options.sqlDelete;
        this.sqlInsert = options.sqlInsert;
        this.sqlUpdate = options.sqlUpdate;

        // 数据源
        String daoName = Ws.sBlank(options.daoName, "default");
        this.auth = WnDaos.loadAuth(io, daoName, sessionVars);

        // 准备 SQL 管理器

        this.sqls = Sqlx.getSqlHolderByPath(io, sessionVars, options.sqlHome);
    }

    @Override
    public WnRoleList queryRolesOf(String grp) {
        NutMap query = Wlang.map("grp", grp);
        return _query_role_list(query);
    }

    private WnRoleList _query_role_list(NutMap filter) {
        NutMap query = new NutMap();
        query.put("filter", filter);
        query.put("limit", 1000);
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlQuery);
        if (null == sqlt) {
            throw Er.create("role.sqlQuery SQL without defined");
        }

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(query, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<WnRoleList>() {
            public WnRoleList doGet(Connection conn) throws SQLException {
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 准备结果集
                List<WnRole> reList = new LinkedList<>();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnRole r = _to_wn_role(bean);
                    reList.add(r);
                }
                return new WnRoleList(reList);
            }
        });
    }

    private WnRole _fetch_role_by_id(String id) {
        NutMap vars = new NutMap();
        vars.put("id", id);
        return _fetch_role_by(vars);
    }

    private WnRole _fetch_role_by_uid_grp(String uid, String grp) {
        NutMap vars = new NutMap();
        vars.put("uid", uid);
        vars.put("grp", grp);
        return _fetch_role_by(vars);
    }

    protected WnRole _fetch_role_by(NutMap vars) {
        // 准备查询语句
        WnSqlTmpl sqlt = this.sqls.get(this.sqlFetch);
        if (null == sqlt) {
            throw Er.create("role.sqlQuery SQL without defined");
        }

        // 渲染 SQL 和参数
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(vars, cps);
        Object[] params = Sqlx.getSqlParamsValue(cps);

        return Sqlx.sqlGet(this.auth, new SqlGetter<WnRole>() {
            public WnRole doGet(Connection conn) throws SQLException {
                PreparedStatement sta = conn.prepareStatement(sql);
                Sqlx.setParmas(sta, params);

                ResultSet rs = sta.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                // 遍历结果集
                while (rs.next()) {
                    NutBean bean = Sqlx.toBean(rs, meta);
                    WnRole r = _to_wn_role(bean);
                    return r;
                }
                return null;
            }
        });
    }

    @Override
    protected List<WnRole> _get_roles(String uid) {
        NutMap query = Wlang.map("uid", uid);
        return _query_role_list(query);
    }

    private NutMap __to_bean_for_upsert(String uid,
                                        String grp,
                                        WnRoleType type,
                                        String unm,
                                        Date now) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        // 转换为一个 Bean
        NutMap bean = new NutMap();
        bean.put("grp", grp);
        bean.put("uid", uid);

        bean.put("unm", unm);
        bean.put("type", type.toString());
        bean.put("role", type.getValue());
        bean.put("ct", Wtime.formatUTC(now, fmt));
        bean.put("lm", Wtime.formatUTC(now, fmt));
        return bean;
    }

    @Override
    protected WnRole _add_role(String uid, String grp, WnRoleType type, String unm) {
        Date now = new Date();
        String roleId = IdMaker.make(now, null);

        NutMap bean = __to_bean_for_upsert(uid, grp, type, unm, now);
        bean.put("id", roleId);

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlInsert);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, bean));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToAdd", Json.toJson(bean));
        }

        return _fetch_role_by_id(roleId);
    }

    @Override
    protected WnRole _set_role(String uid, String grp, WnRoleType type, String unm) {
        Date now = new Date();

        NutMap bean = __to_bean_for_upsert(uid, grp, type, unm, now);
        bean.remove("ct");

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlUpdate);

        // 保存到数据库里
        int count = Sqlx.sqlRun(auth, new SqlAtom(log, sql, bean));
        if (count <= 0) {
            throw Er.create("e.auth.session.FailToUpdate", Json.toJson(bean));
        }

        return _fetch_role_by_uid_grp(uid, grp);
    }

    @Override
    protected void _remove_role(String uid, String grp) {
        // 删除对应缓存
        this.cache.remove(uid);

        // 获取角色
        NutMap query = Wlang.map("grp", grp);
        WnRoleList roles = _query_role_list(query);

        // 准备条件
        List<NutBean> varList = new ArrayList<>(roles.size());
        for (WnRole r : roles) {
            // 删除条件
            NutMap vars = Wlang.map("id", r.getId());
            varList.add(vars);
        }

        // 获取 SQL
        WnSqlTmpl sql = sqls.get(sqlDelete);

        // 执行批量删除
        Sqlx.sqlRun(auth, new SqlBatchAtom(log, sql, varList));
    }
}
