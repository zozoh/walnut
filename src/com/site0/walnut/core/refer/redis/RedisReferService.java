package com.site0.walnut.core.refer.redis;

import java.util.Set;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;

/**
 * 这个是个 Redis 维护的引用集合。
 * <p>
 * 它占有 Redis 的一个数据库，默认键形为 "io:ref:$ID"，值是SET
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RedisReferService implements WnReferApi {

    private String prefix;

    private WedisConfig conf;

    public RedisReferService(WedisConfig conf) {
        this("io:ref:", conf);
    }

    public RedisReferService(String prefix, WedisConfig conf) {
        this.prefix = prefix;
        this.conf = conf;
    }

    @Override
    public long add(String targetId, String... referIds) {
        String key = _KEY(targetId);
        return Wedis.runGet(conf, jed -> {
            jed.sadd(key, referIds);
            return jed.scard(key);
        });
    }

    @Override
    public long remove(String targetId, String... referIds) {
        String key = _KEY(targetId);
        return Wedis.runGet(conf, jed -> {
            jed.srem(key, referIds);
            return jed.scard(key);
        });
    }

    @Override
    public long count(String targetId) {
        String key = _KEY(targetId);
        return Wedis.runGet(conf, jed -> {
            return jed.scard(key);
        });
    }

    @Override
    public Set<String> all(String targetId) {
        String key = _KEY(targetId);
        return Wedis.runGet(conf, jed -> {
            return jed.smembers(key);
        });
    }

    @Override
    public void clear(String targetId) {
        String key = _KEY(targetId);
        Wedis.run(conf, jed -> {
            jed.del(key);
        });
    }

    private String _KEY(String hid) {
        return this.prefix + hid;
    }
}
