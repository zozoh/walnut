package org.nutz.walnut.ext.entity.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.ext.redis.WedisRun;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

public class WnRedisScoreService implements ScoreApi {

    private WedisConfig conf;

    public WnRedisScoreService(WedisConfig conf) {
        this.conf = conf;
    }

    @Override
    public long scoreIt(String taId, String uid, long score) {
        String sumKey = "sum:" + taId;
        return Wedis.run(conf, jed -> {
            Long rk = jed.zrank(taId, uid);
            if (rk == null) {
                jed.zadd(taId, score, uid);
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
        String sumKey = "sum:" + taId;
        return Wedis.run(conf, jed -> {
            Long rk = jed.zrank(taId, uid);
            if (null != rk && rk >= 0) {
                long score = (long) ((double) jed.zscore(taId, uid));

                jed.zrem(taId, uid);
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
        return Wedis.run(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? start + limit - 1 : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrangeWithScores(taId, start, stop);
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
        return Wedis.run(conf, jed -> {
            long start = Math.max(skip, 0);
            long stop = limit > 0 ? limit : Long.MAX_VALUE;
            Set<Tuple> set = jed.zrevrangeWithScores(taId, start, stop);
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
        return Wedis.run(conf, jed -> jed.zcard(taId));
    }

    @Override
    public long sum(String taId) {
        String sumKey = "sum:" + taId;
        return Wedis.run(conf, jed -> {
            String ss = jed.get(sumKey);
            if (Strings.isBlank(ss))
                return 0L;
            return Long.parseLong(ss);
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
        String sumKey = "sum:" + taId;

        return Wedis.run(conf, new WedisRun<Long>() {
            public Long run(Jedis jed) {
                long sum = 0;
                String cursor = "0";

                // 计算求和
                while (true) {
                    ScanResult<Tuple> re = jed.zscan(taId, cursor);
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
        return Wedis.run(conf, jed -> {
            Double score = jed.zscore(taId, uid);
            if (null == score)
                return dft;
            return (long) (double) score;
        });
    }

}
