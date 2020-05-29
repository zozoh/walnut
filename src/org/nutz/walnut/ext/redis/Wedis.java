package org.nutz.walnut.ext.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;

import redis.clients.jedis.Jedis;

public abstract class Wedis {

    /**
     * 键为 "host:port[index]", 例如 "127.0.0.1:6379[0]"
     */
    private static Map<String, Jedis> jedis = new HashMap<>();

    public static void exec(WedisConfig conf, Callback<Jedis> run) {
        Jedis jed = get(conf);
        if (!jed.isConnected()) {
            jed.connect();

        }
        run.invoke(jed);
    }

    public static <T> T run(WedisConfig conf, WedisRun<T> run) {
        Jedis jed = get(conf);
        if (!jed.isConnected()) {
            jed.connect();

        }
        return run.run(jed);
    }

    private static Jedis get(WedisConfig conf) {
        String key = conf.toKey();
        Jedis jed = jedis.get(key);
        if (null == jed) {
            synchronized (Wedis.class) {
                jed = jedis.get(key);
                if (null == jed) {
                    jed = new Jedis(conf.getHost(),
                                    conf.getPort(),
                                    conf.getConnectionTimeout(),
                                    conf.getSoTimeout(),
                                    conf.isSsl());
                    // 校验权限
                    if (conf.hasAuth()) {
                        String re = jed.auth(conf.getAuth());
                        if (!"OK".equalsIgnoreCase(re)) {
                            throw Er.create("e.redis.failAuth", conf.toString());
                        }
                    }
                    // 选择索引
                    if (conf.getSelect() > 0) {
                        jed.select(conf.getSelect());
                    }

                    jedis.put(key, jed);
                }
            }
        }
        return jed;
    }

    public static Set<String> loadedUris() {
        return new HashSet<>(jedis.keySet());
    }

}
