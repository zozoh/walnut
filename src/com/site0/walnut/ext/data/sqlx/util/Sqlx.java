package com.site0.walnut.ext.data.sqlx.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.loader.SqlType;
import com.site0.walnut.ext.data.sqlx.loader.WnSqlHolder;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public abstract class Sqlx {

    private static Map<String, SqlHolder> sqlHolders = new HashMap<>();

    public static SqlHolder getSqlHolderByPath(WnSystem sys, String dirPath) {
        return getSqlHolderByPath(sys.io, sys.session.getVars(), dirPath);
    }

    public static SqlHolder getSqlHolderByPath(WnIo io, NutBean vars, String dirPath) {
        String path = Ws.sBlank(dirPath, "~/.sqlx");
        WnObj oDir = Wn.checkObj(io, vars, path);
        return getSqlHolder(io, oDir);
    }

    public static SqlHolder getSqlHolder(WnIo io, WnObj oDir) {
        String key = oDir.id();
        SqlHolder sqls = sqlHolders.get(key);
        if (null == sqls) {
            synchronized (Sqlx.class) {
                sqls = sqlHolders.get(key);
                if (null == sqls) {
                    sqls = new WnSqlHolder(io, oDir);
                    sqlHolders.put(key, sqls);
                }
            }
        }
        return sqls;
    }

    public static <T> T sqlGet(WnDaoAuth auth, SqlGetter<T> callback) {
        DataSource ds = WnDaos.getDataSource(auth);
        Connection conn = null;
        PreparedStatement sta = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            return callback.doGet(conn);
        }
        catch (SQLException e) {
            throw Er.wrap(e);
        }
        finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != sta) {
                    sta.close();
                }
                if (null != conn) {
                    conn.close();
                }
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
    }

    public static int sqlRun(WnDaoAuth auth, SqlAtom callback) {
        DataSource ds = WnDaos.getDataSource(auth);
        Connection conn = null;
        PreparedStatement sta = null;
        try {
            conn = ds.getConnection();
            return callback.exec(conn);
        }
        catch (SQLException e) {
            throw Er.wrap(e);
        }
        finally {
            try {
                if (null != sta) {
                    sta.close();
                }
                if (null != conn) {
                    conn.close();
                }
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
    }

    public static SqlType autoSqlType(String sql) {
        String str = Ws.trim(sql).toUpperCase();
        if (str.startsWith("SELECT")) {
            return SqlType.SELECT;
        }
        if (str.startsWith("UPDATE")) {
            return SqlType.UPDATE;
        }
        if (str.startsWith("DELETE")) {
            return SqlType.DELETE;
        }
        if (str.startsWith("INSERT")) {
            return SqlType.INSERT;
        }
        return SqlType.EXEC;
    }

    public static List<Object[]> getParams(List<NutBean> list, List<SqlParam> params) {
        List<Object[]> re = new ArrayList<>(list.size());
        for (NutBean li : list) {
            Object[] row = new Object[params.size()];
            int x = 0;
            for (SqlParam param : params) {
                String scope = param.getScope();
                NutBean bean = li;
                if (!Ws.isBlank(scope)) {
                    bean = li.getAs(scope, NutMap.class);
                }
                String key = param.getName();
                Object val = bean.get(key);
                row[x++] = val;
            }
            re.add(row);
        }
        return re;
    }

    public static void setParmas(PreparedStatement sta, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object val = params[i];
            sta.setObject(i + 1, val);
        }
    }

    public static List<NutBean> toBeanList(ResultSet rs) throws SQLException {
        List<NutBean> list = new LinkedList<>();
        ResultSetMetaData meta = rs.getMetaData();
        // 遍历结果集
        while (rs.next()) {
            NutBean bean = Sqlx.toBean(rs, meta);
            list.add(bean);
        }
        return list;
    
    }

    public static NutBean toBean(ResultSet rs, ResultSetMetaData meta) throws SQLException {
        NutMap bean = new NutMap();
        int colCount = meta.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            String colName = meta.getColumnLabel(i);
            Object val = rs.getObject(i);
            bean.put(colName, val);
        }
        return bean;
    }

    public static String[] getSqlParamsName(List<SqlParam> list) {
        String[] re = new String[list.size()];
        int i = 0;
        for (SqlParam li : list) {
            re[i++] = li.getName();
        }
        return re;
    }

    public static Object[] getSqlParamsValue(List<SqlParam> list) {
        Object[] re = new Object[list.size()];
        int i = 0;
        for (SqlParam li : list) {
            re[i++] = li.getValue();
        }
        return re;
    }

    public static String escapeSqlValue(String s) {
        return s.replaceAll("'", "''");
    }

    public static String valueToSqlExp(Object val) {
        if (null == val) {
            return "NULL";
        }
        Mirror<?> mi = Mirror.me(val);
    
        // 布尔
        if (mi.isBoolean()) {
            return ((Boolean) val).booleanValue() ? "1" : "0";
        }
    
        // 数字
        if (mi.isNumber()) {
            return val.toString();
        }
    
        // 日期时间
        if (mi.isDateTimeLike()) {
            Calendar c = Wtime.parseAnyCalendar(val);
            return Wtime.format(c, "''yyyy-MM-dd HH:mm:ss''");
        }
    
        // 下面就用字符串来处理
        String str;
    
        // 数组
        if (mi.isArray()) {
            str = Ws.join((Object[]) val, ",");
        }
        // 集合
        else if (mi.isCollection()) {
            str = Ws.join((Collection<?>) val, ",");
        }
        // 复杂对象
        else if (mi.isMap()) {
            str = Json.toJson(val);
        }
        // 默认当作字符串
        else {
            str = val.toString();
        }
    
        // 最后逃逸一下
        return "'" + escapeSqlValue(str) + "'";
    }
}
