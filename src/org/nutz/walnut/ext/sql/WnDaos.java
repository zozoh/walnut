package org.nutz.walnut.ext.sql;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import com.alibaba.druid.pool.DruidDataSource;

public abstract class WnDaos {

    private static final Log log = Logs.get();

    private static Map<String, NutDao> daos = new HashMap<>();

    public static Dao get(WnDaoConfig conf) {
        WnDaoConnectionInfo info = conf.getConnectionInfo();
        String key = info.toKey();
        NutDao dao = daos.get(key);
        if (log.isDebugEnabled()) {
            log.debugf("try-get dao: %s", key);
        }
        if (null == dao) {
            synchronized (WnDaos.class) {
                dao = daos.get(key);
                if (null == dao) {
                    if (log.isDebugEnabled()) {
                        log.debugf("setup dao: %s", key);
                    }
                    DruidDataSource dataSource = new DruidDataSource();
                    dataSource.setUrl(info.getUrl());
                    dataSource.setUsername(info.getUsername());
                    dataSource.setPassword(info.getPassword());
                    dataSource.setMaxActive(info.getMaxActive());
                    dataSource.setMaxWait(info.getMaxWait());
                    dataSource.setTestWhileIdle(info.isTestWhileIdle());

                    // Create Dao
                    dao = new NutDao(dataSource);
                    daos.put(key, dao);
                }
            }
        }
        return dao;
    }

    public static Set<String> loadedKeys() {
        return new HashSet<>(daos.keySet());
    }

    public static void remove(WnDaoConfig conf) {
        String key = conf.getConnectionInfo().toKey();
        NutDao dao = daos.get(key);
        if (null != dao) {
            DataSource ds = dao.getDataSource();
            Streams.safeClose((Closeable) ds);
            daos.remove(key);
        }
    }

    public static WnDaoConfig loadConfig(WnIo io, String path, NutMap vars) {
        String aph = Wn.normalizeFullPath(path, vars);
        WnObj oConf = io.check(null, aph);
        return loadConfig(io, oConf, vars);
    }

    public static WnDaoConfig loadConfig(WnIo io, WnObj oConf, NutMap vars) {
        WnDaoConfig conf = io.readJson(oConf, WnDaoConfig.class);
        String ph = "~/.dao/" + conf.getDaoName() + ".dao.json";
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDao = io.check(null, aph);
        WnDaoConnectionInfo dci = io.readJson(oDao, WnDaoConnectionInfo.class);
        conf.setConnectionInfo(dci);
        return conf;
    }

    public static WnDaoConfig loadConfig(WnIo io, String path, WnAuthSession se) {
        return loadConfig(io, path, se.getVars());
    }

    public static WnDaoConfig loadConfig(WnSystem sys, String path) {
        return loadConfig(sys.io, path, sys.session);
    }

    public static WnDaoConfig loadConfig(WnIo io, WnObj oConf, WnAuthSession se) {
        return loadConfig(io, oConf, se.getVars());
    }

    public static WnDaoConfig loadConfig(WnSystem sys, WnObj oConf) {
        return loadConfig(sys.io, oConf, sys.session);
    }

}
