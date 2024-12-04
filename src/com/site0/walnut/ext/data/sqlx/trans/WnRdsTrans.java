package com.site0.walnut.ext.data.sqlx.trans;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.nutz.log.Log;

import com.site0.walnut.util.Wlog;

/**
 * 本类假想是一个系统全局的事务对象持有容器。 通过单例模式访问
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnRdsTrans {
    
    private static final Log log = Wlog.getCMD();

    /**
     * 根据 TransID 索引的事务对象
     */
    private Map<String, RdsTransItem> map;

    private WnRdsTrans() {
        map = new HashMap<>();
    }

    public synchronized RdsTransItem beginTrans(Connection conn, int timeout) throws SQLException {
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
        RdsTransItem ti = new RdsTransItem(conn, timeout);
        map.put(ti.getTransId(), ti);
        return ti;
    }

    public synchronized void endTrans(String transId) throws SQLException {
        RdsTransItem ti = map.get(transId);
        if (null != ti) {
            Connection conn = ti.getConn();
            if (!conn.isClosed()) {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                    conn.setAutoCommit(true);
                }
            }
            conn.close();
        }
    }

    public synchronized void rollback(String transId) throws SQLException {
        RdsTransItem ti = map.get(transId);
        if (null != ti) {
            Connection conn = ti.getConn();
            if (!conn.isClosed()) {
                conn.rollback();
            }
        }
    }

    public synchronized RdsTransItem getTrans(String transId) {
        return map.get(transId);
    }
}
