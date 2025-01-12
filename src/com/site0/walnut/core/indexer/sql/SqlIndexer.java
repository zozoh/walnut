package com.site0.walnut.core.indexer.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Each;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.core.indexer.AbstractIoDataIndexer;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.processor.ExecProcessor;
import com.site0.walnut.ext.data.sqlx.processor.QueryProcessor;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.util.SqlAtom;
import com.site0.walnut.ext.data.sqlx.util.SqlGet;
import com.site0.walnut.ext.data.sqlx.util.SqlGetter;
import com.site0.walnut.ext.data.sqlx.util.SqlResetSetGetter;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;

public class SqlIndexer extends AbstractIoDataIndexer {

    private static Log log = Wlog.getIO();

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String entityName;

    private NutBean fixedMatch;

    private static final String SD_INSERT = "insert";
    private static final String SD_UPDATE = "update";
    private static final String SD_COUNT = "count";
    private static final String SD_SELECT = "select";
    private static final String SD_FETCH = "fetch";
    private static final String SD_REMOVE = "remove";
    private static final String SD_INC = "inc";

    protected SqlIndexer(WnObj root,
                         MimeMap mimes,
                         WnDaoAuth auth,
                         SqlHolder sqls,
                         String entityName,
                         NutBean fixedMatch) {
        super(root, mimes);
        this.auth = auth;
        this.sqls = sqls;
        this.entityName = entityName;
        this.fixedMatch = fixedMatch;
    }

    private NutMap prepareQuery(WnQuery q) {
        NutMap re = new NutMap();
        re.putAll(q.first());
        if (null != this.fixedMatch) {
            re.putAll(this.fixedMatch);
        }
        return re;
    }

    private NutMap prepareQuery(WnObj o) {
        NutMap re = new NutMap();
        re.put("id", o.OID().getMyId());
        if (null != this.fixedMatch) {
            re.putAll(this.fixedMatch);
        }
        return re;
    }

    private NutMap prepareQuery(String id) {
        NutMap re = new NutMap();
        WnObjId oid = new WnObjId(id);
        re.put("id", oid.getMyId());
        if (null != this.fixedMatch) {
            re.putAll(this.fixedMatch);
        }
        return re;
    }

    @Override
    public WnAggResult aggregate(WnQuery q, WnAggOptions agg) {
        throw Wlang.noImplement();
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        // 检查必须的 SQL
        WnSqlTmpl _inc = sqls.get(SD_INC);
        if (null == _inc) {
            throw Er.create("inc SQL without defined");
        }
        WnSqlTmpl _get = sqls.get(SD_FETCH);
        if (null == _get) {
            throw Er.create("fetch SQL without defined");
        }

        // 准备条件以及上下文变量
        q.setAllToList(fixedMatch);
        NutMap v_get = prepareQuery(q);
        v_get.put("columns", "id," + key);
        NutMap v_exec = prepareQuery(q);
        v_exec.put("key", key);
        v_exec.put("val", val);

        // 执行获取值的操作
        SqlGet<Integer> get = new SqlGet<>(log, _get, v_get, new SqlResetSetGetter<Integer>() {
            public Integer getValue(ResultSet rs) throws SQLException {
                return rs.getInt(key);
            }
        });

        // 执行更新的操作
        SqlAtom inc = new SqlAtom(log, _inc, v_exec);

        return Sqlx.sqlGet(auth, new SqlGetter<Integer>() {
            public Integer doGet(Connection conn) throws SQLException {
                int re = 0;
                // 先执行再获取值
                if (returnNew) {
                    inc.exec(conn);
                    re = get.doGet(conn);
                }
                // 先获取值再执行
                else {
                    re = get.doGet(conn);
                    inc.exec(conn);
                }
                return re;
            }
        });

    }

    @Override
    public <T> T getAs(String id, String key, Class<T> classOfT, T dft) {
        WnSqlTmpl sqlt = sqls.get(SD_FETCH);
        if (null == sqlt) {
            throw Er.create("fetch SQL without defined");
        }
        NutMap vars = prepareQuery(id);
        vars.put("columns", "id," + key);

        SqlGet<Object> get = new SqlGet<>(log, sqlt, vars, new SqlResetSetGetter<Object>() {
            public Object getValue(ResultSet rs) throws SQLException {
                return rs.getObject(key);
            }
        });
        Object re = Sqlx.sqlGet(auth, get);
        return Castors.me().castTo(re, classOfT);
    }

    @Override
    public void delete(WnObj o) {
        // 检查必须的 SQL
        WnSqlTmpl sqlt = sqls.get(SD_REMOVE);
        if (null == sqlt) {
            throw Er.create("remove SQL without defined");
        }

        // 准备上下文变量
        NutMap vars = prepareQuery(o);

        // 准备操作
        SqlAtom atom = new SqlAtom(log, sqlt, vars);

        // 执行
        Sqlx.sqlRun(auth, atom);
    }

    @Override
    public long count(WnQuery q) {
        WnSqlTmpl sqlt = sqls.get(SD_COUNT);
        if (null == sqlt) {
            throw Er.create("fetch SQL without defined");
        }
        NutMap vars = prepareQuery(q);

        SqlGet<Long> get = new SqlGet<>(log, sqlt, vars, new SqlResetSetGetter<Long>() {
            public Long getValue(ResultSet rs) throws SQLException {
                return rs.getLong(1);
            }
        });
        long N = Sqlx.sqlGet(auth, get);
        return N;
    }

    @Override
    public WnObj push(String id, String key, Object val, boolean returnNew) {
        throw Wlang.noImplement();
    }

    @Override
    public void push(WnQuery query, String key, Object val) {
        throw Wlang.noImplement();

    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        throw Wlang.noImplement();
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        throw Wlang.noImplement();
    }

    @Override
    protected WnObj _fetch_by_name(WnObj p, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected WnObj _create(WnIoObj o) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void _set(String id, NutBean map) {
        // TODO Auto-generated method stub

    }

    @Override
    protected WnIoObj _set_by(WnQuery q, NutBean map, boolean returnNew) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected int _each(WnQuery q, WnObj pHint, Each<WnObj> callback) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected WnIoObj _get_by_id(String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
