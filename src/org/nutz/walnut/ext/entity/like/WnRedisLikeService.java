package org.nutz.walnut.ext.entity.like;

import java.util.Set;

import org.nutz.walnut.ext.entity.like.LikeApi;
import org.nutz.walnut.ext.redis.WnRedis;
import org.nutz.walnut.ext.redis.WnRedisConfig;

import redis.clients.jedis.Jedis;

public class WnRedisLikeService implements LikeApi {

    private Jedis red;

    public WnRedisLikeService(WnRedisConfig conf) {
        this.red = WnRedis.get(conf);
    }

    @Override
    public long likeIt(String taId, String... uids) {
        return red.sadd(taId, uids);
    }

    @Override
    public long unlike(String taId, String... uids) {
        return red.srem(taId, uids);
    }

    @Override
    public Set<String> getAll(String taId) {
        return red.smembers(taId);
    }

    @Override
    public long summary(String taId) {
        return red.scard(taId);
    }

    @Override
    public boolean isLike(String taId, String uid) {
        return red.sismember(taId, uid);
    }

}
