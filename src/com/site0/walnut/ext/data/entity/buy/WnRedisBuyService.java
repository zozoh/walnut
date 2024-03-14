package com.site0.walnut.ext.data.entity.buy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.ext.sys.redis.WedisRunGet;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

public class WnRedisBuyService implements BuyApi {

    private WedisConfig conf;

    public WnRedisBuyService(WedisConfig conf) {
        this.conf = conf;
    }

    private String _KEY(String uid) {
        return conf.setup().getString("prefix", "buy:") + uid;
    }

    @Override
    public int buyIt(String uid, String taId, int count, boolean reset) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            int re = 0;
            if (reset) {
                // 清除
                if (count <= 0) {
                    jed.zrem(key, taId);
                }
                // 修改
                else {
                    jed.zadd(key, count, taId);
                    re = (int) ((double) jed.zscore(key, taId));
                    // 如果小于等于 0，表示购物车木有该商品了，删除
                    if (re <= 0) {
                        jed.zrem(key, taId);
                    }
                }
            } else {
                re = (int) ((double) jed.zincrby(key, count, taId));
                if (re <= 0) {
                    jed.zrem(key, taId);
                }
            }
            return re;
        });
    }

    @Override
    public int remove(String uid, String... taIds) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            return (int) (long) jed.zrem(key, taIds);
        });
    }

    @Override
    public boolean clean(String uid) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            return jed.del(key) > 0;
        });
    }

    @Override
    public List<BuyIt> getAll(String uid) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            Set<Tuple> set = jed.zrangeWithScores(key, 0, -1);
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
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            Set<Tuple> set = jed.zrevrangeWithScores(key, 0, -1);
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
        String key = _KEY(uid);
        return (int) (long) Wedis.runGet(conf, jed -> jed.zcard(key));
    }

    @Override
    public int sum(String uid) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, new WedisRunGet<Integer>() {
            public Integer exec(Jedis jed) {
                long sum = 0;
                String cursor = "0";

                // 计算求和
                while (true) {
                    ScanResult<Tuple> re = jed.zscan(key, cursor);
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
    public int getBuy(String uid, String taId, int dft) {
        String key = _KEY(uid);
        return Wedis.runGet(conf, jed -> {
            Double score = jed.zscore(key, taId);
            if (null == score)
                return dft;
            return (int) (double) score;
        });
    }

}
