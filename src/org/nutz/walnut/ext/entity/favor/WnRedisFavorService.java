package org.nutz.walnut.ext.entity.favor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.ext.entity.favor.FavorApi;
import org.nutz.walnut.ext.redis.WnRedis;
import org.nutz.walnut.ext.redis.WnRedisConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

public class WnRedisFavorService implements FavorApi {

    private Jedis red;

    public WnRedisFavorService(WnRedisConfig conf) {
        this.red = WnRedis.get(conf);
    }

    @Override
    public long favorIt(String uid, String... taIds) {
        Map<String, Double> map = new HashMap<>();
        double now = System.currentTimeMillis();
        for (String taId : taIds) {
            map.put(taId, now);
        }
        return red.zadd(uid, map);
    }

    @Override
    public long unfavor(String uid, String... taIds) {
        return red.zrem(uid, taIds);
    }

    @Override
    public List<FavorIt> getAll(String uid, int limit) {
        List<FavorIt> list;
        String cursor = "0";

        // 找全部
        if (limit <= 0) {
            list = new LinkedList<>();
            while (true) {
                ScanResult<Tuple> re = red.zscan(uid, cursor);
                List<Tuple> result = re.getResult();
                for (Tuple tu : result) {
                    FavorIt fi = new FavorIt(tu.getElement(), (long) tu.getScore());
                    list.add(fi);
                }
                if (re.isCompleteIteration()) {
                    break;
                }
                cursor = re.getCursor();
            }
        }
        // 找有限个
        else {
            list = new ArrayList<>(limit);
            ScanParams sp = new ScanParams();
            sp.count(limit);
            while (list.size() < limit) {
                ScanResult<Tuple> re = red.zscan(uid, cursor, sp);
                List<Tuple> result = re.getResult();
                for (Tuple tu : result) {
                    FavorIt fi = new FavorIt(tu.getElement(), (long) tu.getScore());
                    list.add(fi);
                }
                if (re.isCompleteIteration()) {
                    break;
                }
                cursor = re.getCursor();
            }
        }

        return list;
    }

    @Override
    public long summary(String uid) {
        return red.zcard(uid);
    }

    @Override
    public long whenFavor(String uid, String taId) {
        return (long) ((double) red.zscore(uid, taId));
    }

}
