package org.nutz.walnut.ext.sys.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public abstract class Wedis {

    private static final Log log = Wlog.getMAIN();

    private static Map<String, JedisPool> jedis = new HashMap<>();

    private static Jedis get(WedisConfig conf) {
        String key = conf.toKey();

        // 得到连接池
        JedisPool pool = jedis.get(key);
        if (null == pool) {
            synchronized (Wedis.class) {
                pool = jedis.get(key);
                if (null == pool) {
                    GenericObjectPoolConfig<Jedis> poolConfig = conf.getPoolConfig();
                    pool = new JedisPool(poolConfig,
                                         conf.getHost(),
                                         conf.getPort(),
                                         conf.getConnectionTimeout(),
                                         conf.getSoTimeout(),
                                         Strings.sBlank(conf.getPassword(), null),
                                         conf.getDatabase(),
                                         "wedis",
                                         conf.isSsl());
                    jedis.put(key, pool);
                }
            }
        }

        // 建立连接
        Jedis jed = pool.getResource();
        if (!jed.isConnected()) {
            jed.connect();
        }
        return jed;
    }

    public static void run(WedisConfig conf, WedisRun run) {
        Jedis jed = get(conf);
        if (log.isDebugEnabled()) {
            log.debugf("wedis(%s) opened", conf.toString());
        }
        // 建立连接
        if (!jed.isConnected()) {
            jed.connect();
            if (log.isDebugEnabled()) {
                log.debugf("wedis(%s) connected", conf.toString());
            }
        }
        // 执行
        try {
            run.exec(jed);
            if (log.isDebugEnabled()) {
                log.debugf("wedis(%s) run", conf.toString());
            }
        }
        // 打印错误
        catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.errorf("wedis(%s) error: %s", e.getMessage());
            }
            throw Er.wrap(e);
        }
        // 关闭连接
        finally {
            jed.close();
            if (log.isDebugEnabled()) {
                log.debugf("wedis(%s) closed", conf.toString());
            }
        }
    }

    public static <T> T runGet(WedisConfig conf, WedisRunGet<T> run) {
        Jedis jed = get(conf);
        if (log.isDebugEnabled()) {
            log.debugf("wedis(%s) opened", conf.toString());
        }
        // 建立连接
        if (!jed.isConnected()) {
            jed.connect();
            if (log.isDebugEnabled()) {
                log.debugf("wedis(%s) connected", conf.toString());
            }
        }
        // 执行
        try {
            T re = run.exec(jed);
            if (log.isDebugEnabled()) {
                String msg = null == re ? "~nil" : re.toString();
                if (msg.length() > 20) {
                    msg = msg.substring(0, 20) + "...";
                }
                log.debugf("wedis(%s) runGet: %s", conf.toString(), msg);
            }
            return re;
        }
        // 打印错误
        catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.errorf("wedis(%s) error: %s", conf.toString(), e.getMessage());
            }
            throw Er.wrap(e);
        }
        // 关闭连接
        finally {
            jed.close();
            if (log.isDebugEnabled()) {
                log.debugf("wedis(%s) closed", conf.toString());
            }
        }
    }

    public static Set<String> loadedKeys() {
        return new HashSet<>(jedis.keySet());
    }

    public static WedisConfig loadConfig(WnIo io, String path, NutMap vars) {
        String aph = Wn.normalizeFullPath(path, vars);
        WnObj oConf = io.check(null, aph);
        return loadConfig(io, oConf);
    }

    public static WedisConfig loadConfig(WnIo io, WnObj oConf) {
        return io.readJson(oConf, WedisConfig.class);
    }

    public static WedisConfig loadConfig(WnIo io, String path, WnAuthSession se) {
        return loadConfig(io, path, se.getVars());
    }

    public static WedisConfig loadConfig(WnSystem sys, String path) {
        return loadConfig(sys.io, path, sys.session);
    }

    public static WedisConfig loadConfig(WnIo io, WnObj oConf, WnAuthSession se) {
        return loadConfig(io, oConf);
    }

    public static WedisConfig loadConfig(WnSystem sys, WnObj oConf) {
        return loadConfig(sys.io, oConf, sys.session);
    }

}
