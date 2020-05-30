package org.nutz.walnut.ext.entity.favor;

import java.util.Date;

import org.nutz.lang.Times;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.entity.JvmRedisEntityExecutor;
import org.nutz.walnut.ext.entity.RedisEntityPrinter;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_favor extends JvmRedisEntityExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|rever|ms)$");

        String action = params.val_check(0);
        String uid = params.val_check(1);
        int skip = params.getInt("skip", 0);

        // 准备配置
        WedisConfig conf = this.prepareConfig(sys, params, "favor");

        // 准备接口
        FavorApi api = new WnRedisFavorService(conf);

        // 准备返回
        Object re;

        // favor yes
        if ("yes".equals(action)) {
            String[] taIds = params.subvals(2);
            re = api.favorIt(uid, taIds);
        }
        // favor no
        else if ("no".equals(action)) {
            String[] taIds = params.subvals(2);
            re = api.unfavor(uid, taIds);
        }
        // favor all
        else if ("all".equals(action)) {
            int limit = params.getInt("limit", 100);
            if (params.is("rever")) {
                re = api.revAll(uid, skip, limit);
            } else {
                re = api.getAll(uid, skip, limit);
            }
        }
        // favor sum
        else if ("count".equals(action)) {
            re = api.count(uid);
        }
        // favor is
        else if ("when".equals(action)) {
            String taId = params.val_check(2);
            long ams = api.whenFavor(uid, taId);
            // 保持毫秒数
            if (params.is("ms")) {
                re = ams;
            }
            // 转换成可阅读文字
            else {
                String df = params.getString("df", "yyyy-MM-dd HH:mm:ss");
                Date d = Times.D(ams);
                re = Times.format(df, d);
            }
        }
        // 不支持的动作
        else {
            throw Er.create("e.cmd.favor.invalidAction", action);
        }

        // 输出
        String fmt = params.get("out", "%d) %s + %s");
        output(sys, params, re, new RedisEntityPrinter<FavorIt>() {
            public void print(FavorIt fi, int i) {
                sys.out.printlnf(fmt, i, fi.getTimeText(), fi.getTarget());
            }
        });
    }

}
