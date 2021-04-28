package org.nutz.walnut.ext.data.entity.score;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.entity.JvmRedisEntityExecutor;
import org.nutz.walnut.ext.data.entity.RedisEntityPrinter;
import org.nutz.walnut.ext.sys.redis.WedisConfig;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_score extends JvmRedisEntityExecutor<ScoreIt> {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|rever|quiet)$");

        String action = params.val_check(0);
        String taId = params.val_check(1);
        int skip = params.getInt("skip", 0);

        // 准备配置
        WedisConfig conf = this.prepareConfig(sys, params, "score");

        // 准备接口
        ScoreApi api = new WnRedisScoreService(conf);

        // 准备返回
        Object re;

        // score it
        if ("it".equals(action)) {
            String uid = params.val_check(2);
            long score = params.val_check_int(3);
            re = api.scoreIt(taId, uid, score);
        }
        // score cancel
        else if ("cancel".equals(action)) {
            String uid = params.val_check(2);
            re = api.cancel(taId, uid);
        }
        // score all
        else if ("all".equals(action)) {
            int limit = params.getInt("limit", 100);
            if (params.is("rever")) {
                re = api.revAll(taId, skip, limit);
            } else {
                re = api.getAll(taId, skip, limit);
            }
        }
        // score count
        else if ("count".equals(action)) {
            re = api.count(taId);
        }
        // score sum
        else if ("sum".equals(action)) {
            re = api.sum(taId);
        }
        // score avg
        else if ("avg".equals(action)) {
            re = api.avg(taId);
        }
        // score resum
        else if ("resum".equals(action)) {
            re = api.resum(taId);
        }
        // score is
        else if ("get".equals(action)) {
            String uid = params.val_check(2);
            long dft = params.val_check_int(3);
            re = api.getScore(taId, uid, dft);
        }
        // 不支持的动作
        else {
            throw Er.create("e.cmd.score.invalidAction", action);
        }

        // 输出
        if (!params.is("quiet")) {
            String fmt = params.get("out", "%d) %d <- %s");
            output(sys, params, re, new RedisEntityPrinter<ScoreIt>() {
                public void print(ScoreIt it, int i) {
                    sys.out.printlnf(fmt, i++, it.getScore(), it.getName());
                }
            });
        }
    }

}
