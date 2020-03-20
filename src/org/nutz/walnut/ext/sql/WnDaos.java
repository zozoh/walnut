package org.nutz.walnut.ext.sql;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.lang.Streams;

import com.alibaba.druid.pool.DruidDataSource;

public abstract class WnDaos {

    /**
     * 键为 datasource URI, 例如 "root@jdbc:mysql://127.0.0.1:3306/nutzbook"
     */
    protected static Map<String, NutDao> daos = new ConcurrentHashMap<>();

    static String KEY(String url, String username) {
        return username + "@" + url;
    }

    public static Dao getOrCreate(String url, String username, String passwd) {
        String uri = KEY(url, username);
        Dao dao = daos.get(uri);
        if (null == dao) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(passwd);
            dataSource.setMaxActive(50);
            dataSource.setMaxWait(15000);
            dataSource.setTestWhileIdle(true);

            // Create Dao
            NutDao _nd = new NutDao(dataSource);
            daos.put(uri, _nd);

            dao = _nd;
        }
        return dao;
    }

    public static Set<String> loadedUris() {
        return new HashSet<>(daos.keySet());
    }

    public static void remove(String url, String username) {
        String uri = KEY(url, username);
        NutDao dao = daos.get(uri);
        if (null != dao) {
            DataSource ds = dao.getDataSource();
            Streams.safeClose((Closeable) ds);
            daos.remove(uri);
        }
    }

    public static Dao reload(String url, String username, String passwd) {
        remove(url, username);
        return getOrCreate(url, username, passwd);
    }

}
