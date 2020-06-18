package org.nutz.walnut.ext.entity.buy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.ext.redis.WedisRunGet;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

public class WnRedisBuyService implements BuyApi {

    private WedisConfig conf;

    public WnRedisBuyService(WedisConfig conf) {
        this.conf = conf;
    }

    @Override
    public int buyIt(String uid, String taId, int count, boolean reset) {
        return Wedis.runGet(conf, jed -> {
            int re = 0;
            if (reset) {
                // 清除
                if (count <= 0) {
                    jed.zrem(uid, taId);
                }
                // 修改
                else {
                    jed.zadd(uid, count, taId);
                    re = (int) ((double) jed.zscore(uid, taId));
                }
            } else {
                re = (int) ((double) jed.zincrby(uid, count, taId));
                if (re <= 0) {
                    jed.zrem(uid, taId);
                }
            }
            return re;
        });
    }

    @Override
    public int remove(String uid, String... taIds) {
        return Wedis.runGet(conf, jed -> {
            return (int) (long) jed.zrem(uid, taIds);
        });
    }

    @Override
    public boolean clean(String uid) {
        return Wedis.runGet(conf, jed -> {
            return jed.del(uid) > 0;
        });
    }

    @Override
    public List<BuyIt> getAll(String uid) {
        return Wedis.runGet(conf, jed -> {
            Set<Tuple> set = jed.zrangeWithScores(uid, 0, -1);
            List<BuyIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                BuyIt fi = new BuyIt(tu.getElement(), (int) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public List<BuyIt> revAll(String uid) {
        return Wedis.runGet(conf, jed -> {
            Set<Tuple> set = jed.zrevrangeWithScores(uid, 0, -1);
            List<BuyIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                BuyIt fi = new BuyIt(tu.getElement(), (int) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public int count(String uid) {
        return (int) (long) Wedis.runGet(conf, jed -> jed.zcard(uid));
    }

    @Override
    public int sum(String uid) {
        return Wedis.runGet(conf, new WedisRunGet<Integer>() {
            public Integer exec(Jedis jed) {
                long sum = 0;
                String cursor = "0";

                // 计算求和
                while (true) {
                    ScanResult<Tuple> re = jed.zscan(uid, cursor);
                    List<Tuple> result = re.getResult();
                    for (Tuple tu : result) {
                        sum += (long) tu.getScore();

                    }
                    if (re.isCompleteIteration()) {
                        break;
                    }
                    cursor = re.getCursor();
                }

                return (int) sum;
            }
        });

    }

    @Override
    public int getBuy(String taId, String uid, int dft) {
        return Wedis.runGet(conf, jed -> {
            Double score = jed.zscore(taId, uid);
            if (null == score)
                return dft;
            return (int) (double) score;
        });
    }

}
