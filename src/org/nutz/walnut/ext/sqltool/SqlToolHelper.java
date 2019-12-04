package org.nutz.walnut.ext.sqltool;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;

import com.alibaba.druid.pool.DruidDataSource;

public class SqlToolHelper {

    protected static Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    protected static Map<String, Dao> daos = new ConcurrentHashMap<>();

    public static DataSource getDataSource(WnAccount usr, String name) {
        return dataSources.get(key(usr, name));
    }

    public static DataSource getOrCreateDataSource(WnAccount usr, String name, NutMap conf) {
        DataSource ds = getDataSource(usr, name);
        if (ds == null) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(conf.getString("url"));
            dataSource.setUsername(conf.getString("username"));
            dataSource.setPassword(conf.getString("password"));
            dataSource.setMaxActive(50);
            dataSource.setMaxWait(15000);
            dataSource.setTestWhileIdle(true);
            dataSources.put(key(usr, name), dataSource);
            ds = dataSource;
        }
        return ds;
    }

    public static void removeDataSource(WnAccount usr, String name) {
        DataSource ds = getDataSource(usr, name);
        if (ds != null && ds instanceof Closeable) {
            Streams.safeClose((Closeable) ds);
            dataSources.remove(key(usr, name));
        }
    }

    public static String key(WnAccount usr, String name) {
        return usr.getName() + ";" + name;
    }

    public static Set<String> keys() {
        return new HashSet<>(dataSources.keySet());
    }

    public static Dao getDao(WnAccount usr, String name, NutMap conf) {
        Dao dao = daos.get(key(usr, name));
        if (dao == null) {
            DataSource ds = getOrCreateDataSource(usr, name, conf);
            dao = new NutDao(ds);
            daos.put(key(usr, name), dao);
        }
        return dao;
    }

    public static void reload(WnAccount usr, String name, NutMap conf) {
        removeDataSource(usr, name);
        getOrCreateDataSource(usr, name, conf);
    }
}
