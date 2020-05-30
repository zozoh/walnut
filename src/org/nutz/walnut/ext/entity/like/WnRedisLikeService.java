package org.nutz.walnut.ext.entity.like;

import java.util.Set;

import org.nutz.walnut.ext.entity.like.LikeApi;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;

public class WnRedisLikeService implements LikeApi {

    private WedisConfig conf;

    public WnRedisLikeService(WedisConfig conf) {
        this.conf = conf;
    }

    @Override
    public long likeIt(String taId, String... uids) {
        return Wedis.runGet(conf, jed -> jed.sadd(taId, uids));
    }

    @Override
    public long unlike(String taId, String... uids) {
        return Wedis.runGet(conf, jed -> jed.srem(taId, uids));
    }

    @Override
    public Set<String> getAll(String taId) {
        return Wedis.runGet(conf, jed -> jed.smembers(taId));
    }

    @Override
    public long count(String taId) {
        return Wedis.runGet(conf, jed -> jed.scard(taId));
    }

    @Override
    public boolean isLike(String taId, String uid) {
        return Wedis.runGet(conf, jed -> jed.sismember(taId, uid));
    }

}
