package com.site0.walnut.ext.data.entity.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Strings;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.ext.sys.redis.WedisRunGet;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

public class WnRedisScoreService implements ScoreApi {

    private WedisConfig conf;

    public WnRedisScoreService(WedisConfig conf) {
        this.conf = conf;
    }

    private String _KEY(String taId) {
        return conf.setup().getString("prefix", "like:") + taId;
    }

    @Override
    public long scoreIt(String taId, String uid, long score) {
        String key = _KEY(taId);
        String sumKey = "sum_" + key;
        return Wedis.runGet(conf, jed -> {
            Long rk = jed.zrank(key, uid);
            if (rk == null) {
                jed.zadd(key, score, uid);
                return jed.incrBy(sumKey, score);

            }
            // 获取总分
            String ss = jed.get(sumKey);
            if (Strings.isBlank(ss))
                return 0L;
            return Long.parseLong(ss);
        });
    }

    @Override
    public long cancel(String taId, String uid) {
        String key = _KEY(taId);
        String sumKey = "sum_" + key;
        return Wedis.runGet(conf, jed -> {
            Long rk = jed.zrank(key, uid);
            if (null != rk && rk >= 0) {
                long score = (long) ((double) jed.zscore(key, uid));

                jed.zrem(key, uid);
                return jed.decrBy(sumKey, score);
            }
            // 获取总分
            String ss = jed.get(sumKey);
            if (Strings.isBlank(ss))
                return 0L;
            return Long.parseLong(ss);
        });
    }

    @Override
    public List<ScoreIt> getAll(String taId, int skip, int limit) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? start + limit - 1 : -1;
            Set<Tuple> set = jed.zrangeWithScores(key, start, stop);
            List<ScoreIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                ScoreIt fi = new ScoreIt(tu.getElement(), (long) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public List<ScoreIt> revAll(String taId, int skip, int limit) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? limit : -1;
            Set<Tuple> set = jed.zrevrangeWithScores(key, start, stop);
            List<ScoreIt> list = new ArrayList<>(set.size());
            for (Tuple tu : set) {
                ScoreIt fi = new ScoreIt(tu.getElement(), (long) tu.getScore());
                list.add(fi);
            }
            return list;
        });
    }

    @Override
    public long count(String taId) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> jed.zcard(key));
    }

    @Override
    public long sum(String taId) {
        String key = _KEY(taId);
        String sumKey = "sum_" + key;
        return Wedis.runGet(conf, jed -> {
            String ss = jed.get(sumKey);
            if (Strings.isBlank(ss))
                return 0L;
            return Long.parseLong(ss);
        });
    }

    @Override
    public long avg(String taId) {
        String key = _KEY(taId);
        String sumKey = "sum_" + key;
        return Wedis.runGet(conf, jed -> {
            // 获取人数
            long count = jed.zcard(key);

            if (count <= 0L) {
                return 0L;
            }

            // 获取总分
            String ss = jed.get(sumKey);
            long sum = Strings.isBlank(ss) ? 0L : Long.parseLong(ss);

            // 获取平均分
            return sum / count;
        });
    }

    @Override
    public long resum(String taId) {
        // String lua = "local res = 0;"
        // + "local totalCards;"
        // + "local i =0; "
        // + "totalCards = redis.call("
        // + "'zrangebyscore',"
        // + "'inTimeCost',"
        // + "'-inf',"
        // + "'+inf',"
        // + "'withscores');"
        // + "for i,v in ipairs(totalCards) "
        // + "do if i%2 == 0 then "
        // + "res = res + v; "
        // + "end; "
        // + "end;"
        // + "return res;";
        String key = _KEY(taId);
        String sumKey = "sum_" + key;

        return Wedis.runGet(conf, new WedisRunGet<Long>() {
            public Long exec(Jedis jed) {
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

                // 记录求和
                jed.set(sumKey, sum + "");

                return sum;
            }
        });

    }

    @Override
    public long getScore(String taId, String uid, long dft) {
        String key = _KEY(taId);
        return Wedis.runGet(conf, jed -> {
            Double score = jed.zscore(key, uid);
            if (null == score)
                return dft;
            return (long) (double) score;
        });
    }

}
