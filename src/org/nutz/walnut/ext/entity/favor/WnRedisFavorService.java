package org.nutz.walnut.ext.entity.favor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.ext.entity.favor.FavorApi;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;

import redis.clients.jedis.Tuple;

public class WnRedisFavorService implements FavorApi {

    private WedisConfig conf;

    public WnRedisFavorService(WedisConfig conf) {
        this.conf = conf;
    }

    @Override
    public long favorIt(String uid, String... taIds) {
        Map<String, Double> map = new HashMap<>();
        double now = System.currentTimeMillis();
        for (String taId : taIds) {
            map.put(taId, now);
        }
        return Wedis.runGet(conf, jed -> jed.zadd(uid, map));
    }

    @Override
    public long unfavor(String uid, String... taIds) {
        return Wedis.runGet(conf, jed -> jed.zrem(uid, taIds));
    }

    @Override
    public List<FavorIt> getAll(String uid, int skip, int limit) {
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? start + limit - 1 : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrangeWithScores(uid, start, stop);
            List<FavorIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                FavorIt fi = new FavorIt(tu.getElement(), (long) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public List<FavorIt> revAll(String uid, int skip, int limit) {
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? limit : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrevrangeWithScores(uid, start, stop);
            List<FavorIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                FavorIt fi = new FavorIt(tu.getElement(), (long) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public long count(String uid) {
        return Wedis.runGet(conf, jed -> jed.zcard(uid));
    }

    @Override
    public long whenFavor(String uid, String taId) {
        return Wedis.runGet(conf, jed -> (long) ((double) jed.zscore(uid, taId)));
    }

}
