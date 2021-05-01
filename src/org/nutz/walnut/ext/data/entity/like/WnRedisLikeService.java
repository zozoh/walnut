package org.nutz.walnut.ext.data.entity.like;

import java.util.Set;

import org.nutz.walnut.ext.data.entity.like.LikeApi;
import org.nutz.walnut.ext.sys.redis.Wedis;
import org.nutz.walnut.ext.sys.redis.WedisConfig;

public class WnRedisLikeService implements LikeApi {

    private WedisConfig conf;

    public WnRedisLikeService(WedisConfig conf) {
        this.conf = conf;
    }

    private String _KEY(String taId) {
        return conf.setup().getString("prefix", "like:") + taId;
    }

    @Override
    public long likeIt(String taId, String... uids) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> jed.sadd(key, uids));
    }

    @Override
    public long unlike(String taId, String... uids) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> jed.srem(key, uids));
    }

    @Override
    public Set<String> getAll(String taId) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> jed.smembers(key));
    }

    @Override
    public long count(String taId) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> jed.scard(key));
    }

    @Override
    public boolean isLike(String taId, String uid) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> {
            Boolean rb = jed.sismember(key, uid);
            return null == rb ? false : rb;
        });
    }

}
