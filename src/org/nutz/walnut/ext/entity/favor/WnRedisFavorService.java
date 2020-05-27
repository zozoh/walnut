package org.nutz.walnut.ext.entity.favor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.Callback;
import org.nutz.walnut.ext.entity.favor.FavorApi;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
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
        return Wedis.run(conf, jed -> jed.zadd(uid, map));
    }

    @Override
    public long unfavor(String uid, String... taIds) {
        return Wedis.run(conf, jed -> jed.zrem(uid, taIds));
    }

    @Override
    public List<FavorIt> getAll(String uid, int limit) {
        List<FavorIt> list = new LinkedList<>();

        Wedis.exec(conf, new Callback<Jedis>() {
            public void invoke(Jedis jed) {
                String cursor = "0";
                // 找全部
                if (limit <= 0) {
                    while (true) {
                        ScanResult<Tuple> re = jed.zscan(uid, cursor);
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
                    ScanParams sp = new ScanParams();
                    sp.count(limit);
                    while (list.size() < limit) {
                        ScanResult<Tuple> re = jed.zscan(uid, cursor, sp);
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
            }
        });

        return list;
    }

    @Override
    public long summary(String uid) {
        return Wedis.run(conf, jed -> jed.zcard(uid));
    }

    @Override
    public long whenFavor(String uid, String taId) {
        return Wedis.run(conf, jed -> (long) ((double) jed.zscore(uid, taId)));
    }

}
