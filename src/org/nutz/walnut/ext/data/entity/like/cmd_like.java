package org.nutz.walnut.ext.data.entity.like;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.entity.JvmRedisEntityExecutor;
import org.nutz.walnut.ext.data.entity.RedisEntityPrinter;
import org.nutz.walnut.ext.sys.redis.WedisConfig;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_like extends JvmRedisEntityExecutor<String> {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|quiet)$");

        String action = params.val_check(0);
        String taId = params.val_check(1);

        // 准备配置
        WedisConfig conf = this.prepareConfig(sys, params, "like");

        // 准备接口
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
        if (!params.is("quiet")) {
            String fmt = params.get("out", "%d) %s");
            output(sys, params, re, new RedisEntityPrinter<String>() {
                public void print(String it, int i) {
                    sys.out.printlnf(fmt, i, it);
                }
            });
        }
    }

}
