package com.site0.walnut.core.bm.sql;

import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.core.bm.BMSwapFiles;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.processor.ExecProcessor;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public class SqlBM extends AbstractIoBM {

    private static final Log log = Wlog.getIO();

    BMSwapFiles swaps;

    private WnDaoAuth auth;

    private SqlHolder sqls;

    private String entityName;

    private ExecProcessor exec;

    public SqlBM(WnIoHandleManager handles,
                 String phSwap,
                 WnDaoAuth auth,
                 SqlHolder sqls,
                 String entityName) {
        super(handles);

        // 获取交换区目录
        this.swaps = BMSwapFiles.create(phSwap, true);

        // 记录其他信息
        this.auth = auth;
        this.sqls = sqls;
        this.entityName = entityName;

        // 准备执行器
        this.exec = new ExecProcessor(log);
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm) {
            return true;
        }
        if (null == bm
            || null == this.swaps
            || null == this.auth
            || null == this.sqls
            || null == this.entityName) {
            return false;
        }
        if (bm instanceof SqlBM) {
            SqlBM _bm = (SqlBM) bm;
            if (!this.swaps.equals(_bm.swaps)) {
                return false;
            }
            if (!this.auth.equals(_bm.auth)) {
                return false;
            }
            if (!this.sqls.equals(_bm.sqls)) {
                return false;
            }
            return this.entityName.equals(_bm.entityName);
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.isRead(mode)) {
            return new SqlBMReadHandle(this);
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new SqlBMWriteHandle(this);
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            return new SqlBMReadWriteHandle(this);
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new SqlBMReadWriteHandle(this);
        }
        throw Er.create("e.io.bm.SqlBM.NonsupportMode", mode);
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        return 0;
    }

    @Override
    public long remove(WnObj o) {
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        return 0;
    }

    public void writeBlob(WnObj obj, File swapFile) {
        String sha1 = Wlang.sha1(swapFile);
        long olen = swapFile.length();
        String sqlName;

        // 首先查询一下这个文件的内容是否存在，
        int count = countObj(obj);

        // 不存在，就用创建
        if (count == 0) {
            sqlName = getSqlName("insert_content");
        }
        // 存在就更新，当然 count==-1 表示用户并未定义 count_content 操作
        // 因为他这时候应该希望 bm/index 在一个表里，因此直接 update 就好
        else {
            sqlName = getSqlName("update_content");
        }

        // 准备输入流
        byte[] content = Files.readBytes(swapFile);

        String objId = obj.OID().getMyId();
        WnSqlTmpl sqlt = sqls.get(sqlName);

        String now = (String) Wn.fmt_str_macro("%date:now");

        NutBean record = Wlang.map("id", objId);
        record.put("nm", obj.name());
        record.put("tp", obj.type());
        record.put("mime", obj.mime());
        record.put("sha1", sha1);
        record.put("len", olen);
        record.put("ct", now);
        record.put("lm", now);
        record.put("content", content);

        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(record, cps);
        Object[] sqlParams = WnSqls.getSqlParamsValue(cps);

        try {
            SqlExecResult re = _sql_get(new SqlBMGetter<SqlExecResult>() {
                public SqlExecResult doGet(Connection conn) throws SQLException {
                    PreparedStatement sta = conn.prepareStatement(sql);
                    WnSqls.setParmas(sta, sqlParams);
                    return exec.runWithParams(conn, sql, sqlParams);
                }
            });
            if (re.updateCount <= 0) {
                log.warnf("SqlBM writeBlob Fail: objId=%s, sqlName=%s, sql=%s, record=%s",
                          objId,
                          sqlName,
                          sql,
                          Json.toJson(record));
            }
        }
        catch (RuntimeException e) {
            String msg = String.format("SqlBM writeBlob Fail: objId=%s, sqlName=%s, sql=%s, record=%s",
                                       objId,
                                       sqlName,
                                       sql,
                                       Json.toJson(record));
            log.warn(msg, e);
            throw e;
        }

    }

    private String getSqlName(String name) {
        return this.entityName + "." + name;
    }

    public int countObj(WnObj obj) {
        return _sql_get(new SqlBMGetter<Integer>() {
            public Integer doGet(Connection conn) throws SQLException {
                ResultSet rs = getResultSet(conn, obj, "count_content");
                if (null == rs) {
                    return -1;
                }
                return rs.getInt("total");
            }

        });
    }

    public Blob getBlob(WnObj obj) {
        return _sql_get(new SqlBMGetter<Blob>() {
            public Blob doGet(Connection conn) throws SQLException {
                ResultSet rs = getResultSet(conn, obj, "fetch_content");
                if (null == rs) {
                    throw Er.create("e.io.SqlBM.getBlob.FailResultSet", obj);
                }
                return rs.getBlob("content");
            }

        });
    }

    private ResultSet getResultSet(Connection conn, WnObj obj, String sqlSubName)
            throws SQLException {
        String objId = obj.OID().getMyId();
        String sqlName = getSqlName(sqlSubName);
        WnSqlTmpl sqlt = sqls.get(sqlName);
        if (null == sqlt) {
            return null;
        }

        NutBean filter = Wlang.map("id", objId);
        List<SqlParam> cps = new ArrayList<>();
        String sql = sqlt.render(filter, cps);
        Object[] sqlParams = WnSqls.getSqlParamsValue(cps);

        if (log.isInfoEnabled()) {
            log.infof("SqlBM: sqlName=%s, objId=%s, sql=%s, params=%s",
                      sqlName,
                      objId,
                      sql,
                      Json.toJson(sqlParams));
        }

        PreparedStatement sta = conn.prepareStatement(sql);
        WnSqls.setParmas(sta, sqlParams);

        ResultSet rs = sta.executeQuery();
        if (rs.next()) {
            return rs;
        }
        return null;
    }

    private <T> T _sql_get(SqlBMGetter<T> callback) {
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
}
