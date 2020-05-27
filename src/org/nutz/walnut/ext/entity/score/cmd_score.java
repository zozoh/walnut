package org.nutz.walnut.ext.entity.score;

import java.util.Collection;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

public class cmd_score extends JvmExecutor {

    @SuppressWarnings("unchecked")
    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|rever)$");

        String action = params.val_check(0);
        String taId = params.val_check(1);
        int skip = params.getInt("skip", 0);

        // 准备配置
        String nmConf = params.get("conf", "_score");
        if (!nmConf.endsWith(".json")) {
            nmConf += ".json";
        }
        String phConf = "~/.domain/score/" + nmConf;
        WnObj oConf = Wn.checkObj(sys, phConf);

        // 准备接口
        WedisConfig conf = sys.io.readJson(oConf, WedisConfig.class);
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
        boolean asJson = params.is("json");
        if (params.is("ajax")) {
            re = Ajax.ok().setData(re);
            asJson = true;
        }

        if (asJson) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(re, jfmt));
        }
        // 输出集合
        else if (re instanceof Collection<?>) {
            Collection<ScoreIt> co = (Collection<ScoreIt>) re;
            if (co.isEmpty()) {
                sys.out.println("(~nil~)");
            } else {
                int i = params.getInt("i", 1) + skip;
                String fmt = params.get("out", "%d) %d <- %s");
                for (ScoreIt fi : co) {
                    sys.out.printlnf(fmt, i++, fi.getScore(), fi.getName());
                }
            }
        }
        // 直接输出把
        else {
            sys.out.println(re.toString());
        }
    }

}
