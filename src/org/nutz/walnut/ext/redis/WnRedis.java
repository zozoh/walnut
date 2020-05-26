package org.nutz.walnut.ext.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.api.err.Er;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public abstract class WnRedis {

    /**
     * 键为 "host:port", 例如 "127.0.0.1:6379"
     */
    protected static Map<String, JedisPool> pools = new HashMap<>();

    public static Jedis get(WnRedisConfig conf) {
        JedisPool pool = getOrCreate(conf);
        if (null == pool) {
            throw Er.create("e.redis.failConnected", conf.toString());
        }
        Jedis jed = pool.getResource();
        if (conf.hasAuth()) {
            String re = jed.auth(conf.getAuth());
            if (!"OK".equalsIgnoreCase(re)) {
                throw Er.create("e.redis.failAuth", conf.toString());
            }
        }
        if (conf.getSelect() > 0) {
            jed.select(conf.getSelect());
        }
        return jed;
    }

    public static JedisPool getOrCreate(WnRedisConfig conf) {
        String key = conf.toKey();
        JedisPool pool = pools.get(key);
        if (null == pool) {
            synchronized (WnRedis.class) {
                pool = pools.get(key);
                if (null == pool) {
                    pool = new JedisPool(conf.getHost(), conf.getPort(), conf.isSsl());
                    pools.put(key, pool);
                }
            }
        }
        return pool;
    }

    public static JedisPool getOrCreate(String host, int port, boolean ssl) {
        WnRedisConfig conf = new WnRedisConfig(host, port, ssl);
        return getOrCreate(conf);
    }

    public static Set<String> loadedUris() {
        return new HashSet<>(pools.keySet());
    }

    public static boolean remove(WnRedisConfig conf) {
        String key = conf.toKey();
        if (pools.containsKey(key)) {
            synchronized (WnRedis.class) {
                JedisPool pool = pools.get(key);
                if (null != pool) {
                    pool.destroy();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean remove(String host, int port) {
        return remove(new WnRedisConfig(host, port, false));
    }

    public static JedisPool reload(String host, int port, boolean ssl) {
        WnRedisConfig conf = new WnRedisConfig(host, port, ssl);
        return reload(conf);
    }

    public static JedisPool reload(WnRedisConfig conf) {
        synchronized (WnRedis.class) {
            remove(conf);
            return getOrCreate(conf);
        }
    }

}
