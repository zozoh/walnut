package com.site0.walnut.web2.service.register;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 4 份 Redis 配置（旧 {@code conf/ioc/redis.js}）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnRedisRegistrar {

    private WnRedisRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();

        factory.register("redisConfForIoRefers",
                         __redisConf(conf, "redis-db-refer", 0, null));
        factory.register("redisConfForIoBM", __redisConf(conf, "redis-db-bm", 1, null));
        factory.register("redisConfForIoHandle",
                         __redisConf(conf, "redis-db-handle", 2, null));

        NutMap lockSetup = new NutMap();
        lockSetup.put("prefix", conf.get("lock-redis-prefix", "lock:"));
        lockSetup.put("retry-interval", conf.getLong("lock-ask-retry-interval", 100));
        lockSetup.put("retry-times", conf.getInt("lock-ask-retry-times", 5));
        factory.register("redisConfForLockApi",
                         __redisConf(conf, "redis-db-lock", 2, lockSetup));
    }

    private static WedisConfig __redisConf(WnConfig conf,
                                           String dbKey,
                                           int dftDb,
                                           NutMap setup) {
        WedisConfig rc = new WedisConfig();
        rc.setHost(conf.get("redis-host"));
        rc.setPort(conf.getInt("redis-port"));
        rc.setSsl(conf.getBoolean("redis-ssl", false));
        rc.setPassword(conf.get("redis-password"));
        rc.setDatabase(conf.getInt(dbKey, dftDb));
        if (null != setup) {
            rc.setSetup(setup);
        }
        return rc;
    }

}
