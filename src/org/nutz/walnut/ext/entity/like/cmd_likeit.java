package org.nutz.walnut.ext.entity.like;

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

public class cmd_likeit extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json)$");

        String action = params.val_check(0);
        String taId = params.val_check(1);

        // 准备配置
        String nmConf = params.get("conf", "_like");
        if (!nmConf.endsWith(".json")) {
            nmConf += ".json";
        }
        String phConf = "~/.domain/like/" + nmConf;
        WnObj oConf = Wn.checkObj(sys, phConf);

        // 准备接口
        WedisConfig conf = sys.io.readJson(oConf, WedisConfig.class);
        LikeApi api = new WnRedisLikeService(conf);

        // 准备返回
        Object re;

        // likeit yes
        if ("yes".equals(action)) {
            String[] uids = params.subvals(2);
            re = api.likeIt(taId, uids);
        }
        // likeit no
        else if ("no".equals(action)) {
            String[] uids = params.subvals(2);
            re = api.unlike(taId, uids);
        }
        // likeit all
        else if ("all".equals(action)) {
            re = api.getAll(taId);
        }
        // likeit sum
        else if ("count".equals(action)) {
            re = api.count(taId);
        }
        // likeit is
        else if ("is".equals(action)) {
            String uid = params.val_check(2);
            re = api.isLike(taId, uid);
        }
        // 不支持的动作
        else {
            throw Er.create("e.cmd.likeit.invalidAction", action);
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
            Collection<?> co = (Collection<?>) re;
            if (co.isEmpty()) {
                sys.out.println("(~nil~)");
            } else {
                int i = params.getInt("i", 1);
                String fmt = params.get("out", "%d) %s");
                for (Object it : co) {
                    sys.out.printlnf(fmt, i++, it.toString());
                }
            }

        }
        // 直接输出把
        else {
            sys.out.println(re.toString());
        }
    }

}
