package org.nutz.walnut.ext.data.entity.favor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.walnut.ext.data.entity.favor.FavorApi;
import org.nutz.walnut.ext.sys.redis.Wedis;
import org.nutz.walnut.ext.sys.redis.WedisConfig;
import org.nutz.walnut.util.Wn;

import redis.clients.jedis.Tuple;

public class WnRedisFavorService implements FavorApi {

    private WedisConfig conf;

    public WnRedisFavorService(WedisConfig conf) {
        this.conf = conf;
    }
    
    private String _KEY(String uid) {
        return conf.setup().getString("prefix", "favor:") + uid;
    }

    @Override
    public long favorIt(String uid, String... taIds) {
        String key = _KEY(uid);
        Map<String, Double> map = new HashMap<>();
        double now = Wn.now();
        for (String taId : taIds) {
            map.put(taId, now);
        }
        return Wedis.runGet(conf, jed -> jed.zadd(key, map));
    }

    @Override
    public long unfavor(String uid, String... taIds) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> jed.zrem(key, taIds));
    }

    @Override
    public List<FavorIt> getAll(String uid, int skip, int limit) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? start + limit - 1 : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrangeWithScores(key, start, stop);
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
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? limit - 1 : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrevrangeWithScores(key, start, stop);
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
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> jed.zcard(key));
    }

    @Override
    public long[] whenFavor(String uid, String... taIds) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            long[] re = new long[taIds.length];
            for (int i = 0; i < taIds.length; i++) {
                String taId = taIds[i];
                Double score = jed.zscore(key, taId);
                if (null == score) {
                    re[i] = 0L;
                } else {
                    re[i] = (long) ((double) score);
                }
            }
            return re;
        });
    }

}
