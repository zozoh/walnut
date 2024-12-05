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

    /**
     * 回收一个过期的连接
     * 
     * @return 过期事务里对应的连接
     */
    public synchronized Connection recycleExpiredConnection() {
        if (map.isEmpty()) {
            return null;
        }
        // 获得系统当前时间
        long now = System.currentTimeMillis();

        // 尝试获取过期的连接
        for (RdsTransItem ti : map.values()) {
            if (ti.isExpiredFor(now)) {
                map.remove(ti.getTransId());
                return ti.getConn();
            }
        }

        // 就是没有了
        return null;
    }

    public synchronized RdsTransItem beginTrans(Connection conn, int timeout) {
        if (log.isInfoEnabled()) {
            log.infof("beginTrans: timeout=%s", timeout);
        }
        try {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
        }
        catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to setAutoCommit(false)", e);
            }
        }
        RdsTransItem ti = new RdsTransItem(conn, timeout);
        map.put(ti.getTransId(), ti);
        if (log.isInfoEnabled()) {
            log.infof("beginTrans: trans=%s", ti);
        }
        return ti;
    }

    public synchronized void endTrans(String transId) {
        if (log.isInfoEnabled()) {
            log.infof("endTrans: %s", transId);
        }
        RdsTransItem ti = map.get(transId);
        if (null != ti) {
            Connection conn = ti.getConn();
            try {
                if (!conn.isClosed()) {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                        conn.setAutoCommit(true);
                    }
                }
                conn.close();
            }
            catch (SQLException e) {
                if (log.isWarnEnabled()) {
                    log.warnf("fail to close conn", e);
                }
            }
            map.remove(transId);
        }
    }

    public synchronized void rolbackTrans(String transId) {
        if (log.isInfoEnabled()) {
            log.infof("rolbackTrans: %s", transId);
        }
        RdsTransItem ti = map.get(transId);
        if (null != ti) {
            Connection conn = ti.getConn();
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                } else if (log.isInfoEnabled()) {
                    log.info("conn is closed already, can not rollback");
                }
            }
            catch (SQLException e) {
                if (log.isWarnEnabled()) {
                    log.warnf("fail to rollback", e);
                }
            }
        }
    }

    public synchronized RdsTransItem getTrans(String transId) {
        return map.get(transId);
    }
}
