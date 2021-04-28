package org.nutz.walnut.core.cache.redis;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoCache;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.sys.redis.Wedis;
import org.nutz.walnut.ext.sys.redis.WedisConfig;

/**
 * 这个是个 Redis 维护的引用集合。
 * <p>
 * 它占有 Redis 的一个数据库，默认键形为 "io:cah:$ID"，值是STRING
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RedisIoCache implements WnIoCache {

    private String prefix;

    private WedisConfig conf;

    /**
     * 默认对象缓冲持续时间（秒）
     * <p>
     * 0或负数表示永不过期
     */
    private int timeout;

    public RedisIoCache(WedisConfig conf) {
        this("io:cah:", conf, 600000); // 默认搞个10分钟
    }

    public RedisIoCache(WedisConfig conf, int timeout) {
        this("io:cah:", conf, timeout);
    }

    public RedisIoCache(String prefix, WedisConfig conf, int timeout) {
        this.prefix = prefix;
        this.conf = conf;
        this.timeout = timeout;
    }

    @Override
    public WnObj get(String id) {
        String key = _KEY(id);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (null != json) {
            WnIoObj obj = Json.fromJson(WnIoObj.class, json);
            this.touch(obj);
            return obj;
        }
        return null;
    }

    @Override
    public WnObj fetch(String aph) {
        String key = _KEY(aph);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (null != json) {
            WnIoObj obj = Json.fromJson(WnIoObj.class, json);
            this.touch(obj);
            return obj;
        }
        return null;
    }

    @Override
    public void remove(String id) {
        String key = _KEY(id);
        String json = Wedis.runGet(conf, jed -> {
            return jed.get(key);
        });
        if (null != json) {
            WnIoObj obj = Json.fromJson(WnIoObj.class, json);
            remove(obj);
        }
    }

    private void remove(WnObj obj) {
        String k1 = _KEY(obj.id());
        String k2 = _KEY(obj.getRegularPath());
        Wedis.run(conf, jed -> {
            jed.del(k1, k2);
        });
    }

    @Override
    public void touch(WnObj obj) {
        // 已经过期
        if (obj.isExpired()) {
            this.remove(obj);
        }
        // 准备两个键
        String k1 = _KEY(obj.id());
        String k2 = _KEY(obj.getRegularPath());
        // 指定了过期时间
        long expi = obj.expireTime();
        if (expi > 0) {
            Wedis.run(conf, jed -> {
                jed.expireAt(k1, expi);
                jed.expireAt(k2, expi);
            });
        }
        // 采用默认过期时间
        else if (timeout > 0) {
            Wedis.run(conf, jed -> {
                jed.expire(k1, timeout);
                jed.expire(k2, timeout);
            });
        }
    }

    @Override
    public void save(WnObj obj) {
        if (!obj.isExpired()) {
            String k1 = _KEY(obj.id());
            String k2 = _KEY(obj.getRegularPath());
            String json = Json.toJson(obj, JsonFormat.compact().setQuoteName(false));
            Wedis.run(conf, jed -> {
                jed.set(k1, json);
                jed.set(k2, json);
            });
            this.touch(obj);
        }
    }

    private String _KEY(String key) {
        return this.prefix + key;
    }

}
