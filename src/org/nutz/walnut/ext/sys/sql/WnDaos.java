package org.nutz.walnut.ext.sys.sql;

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
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import com.alibaba.druid.pool.DruidDataSource;

public abstract class WnDaos {

    private static final Log log = Wlog.getCMD();

    private static Map<String, DataSource> dss = new HashMap<>();

    private static Map<String, NutDao> daos = new HashMap<>();

    public static DataSource getDataSource(WnDaoConfig conf) {
        WnDaoAuth info = conf.getAuth();
        String key = info.toKey();
        DataSource ds = dss.get(key);
        if (null == ds) {
            synchronized (WnDaos.class) {
                ds = __get_or_create_datasource(info, key);
            }
        }
        return ds;
    }

    private static DataSource __get_or_create_datasource(WnDaoAuth info, String key) {
        DataSource ds;
        ds = dss.get(key);
        if (null == ds) {
            if (log.isDebugEnabled()) {
                log.debugf("setup datasource: %s", key);
            }
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(info.getUrl());
            dataSource.setUsername(info.getUsername());
            dataSource.setPassword(info.getPassword());
            dataSource.setMaxActive(info.getMaxActive());
            dataSource.setMaxWait(info.getMaxWait());
            dataSource.setTestWhileIdle(info.isTestWhileIdle());

            dss.put(key, dataSource);
            ds = dataSource;
        }
        return ds;
    }

    public static Dao get(WnDaoAuth auth) {
        String key = auth.toKey();
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
                    DataSource ds = __get_or_create_datasource(auth, key);

                    // Create Dao
                    dao = new NutDao(ds);
                    daos.put(key, dao);
                }
            }
        }
        // 搞定
        return dao;
    }

    public static Set<String> loadedKeys() {
        return new HashSet<>(daos.keySet());
    }

    public static void remove(WnDaoConfig conf) {
        String key = conf.getAuth().toKey();
        NutDao dao = daos.get(key);
        if (null != dao) {
            DataSource ds = dao.getDataSource();
            Streams.safeClose((Closeable) ds);
            daos.remove(key);
            dss.remove(key);
        }
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnIo io,
                                                           String path,
                                                           NutMap vars) {
        String aph = Wn.normalizeFullPath(path, vars);
        WnObj oConf = io.check(null, aph);
        return loadConfig(configType, io, oConf, vars);
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnIo io,
                                                           WnObj oConf,
                                                           NutMap vars) {
        T conf = io.readJson(oConf, configType);
        String ph = "~/.dao/" + conf.getDaoName() + ".dao.json";
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDao = io.check(null, aph);
        WnDaoAuth dci = io.readJson(oDao, WnDaoAuth.class);
        conf.setAuth(dci);
        return conf;
    }

    public static WnDaoAuth loadAuth(WnIo io, String daoName, NutMap vars) {
        String ph = "~/.dao/" + daoName + ".dao.json";
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDao = io.check(null, aph);
        return io.readJson(oDao, WnDaoAuth.class);
    }

    public static WnDaoAuth loadAuth(WnSystem sys, String daoName) {
        return loadAuth(sys.io, daoName, sys.session.getVars());
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnIo io,
                                                           String path,
                                                           WnAuthSession se) {
        return loadConfig(configType, io, path, se.getVars());
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnSystem sys,
                                                           String path) {
        return loadConfig(configType, sys.io, path, sys.session);
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnIo io,
                                                           WnObj oConf,
                                                           WnAuthSession se) {
        return loadConfig(configType, io, oConf, se.getVars());
    }

    public static <T extends WnDaoConfig> T loadConfig(Class<T> configType,
                                                           WnSystem sys,
                                                           WnObj oConf) {
        return loadConfig(configType, sys.io, oConf, sys.session);
    }

}
