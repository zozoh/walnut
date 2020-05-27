package org.nutz.walnut.ext.entity.favor;

import java.util.Collection;
import java.util.Date;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Times;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

public class cmd_favor extends JvmExecutor {

    @SuppressWarnings("unchecked")
    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|ms)$");

        String action = params.val_check(0);
        String uid = params.val_check(1);

        // 准备配置
        String nmConf = params.get("conf", "_favor");
        if (!nmConf.endsWith(".json")) {
            nmConf += ".json";
        }
        String phConf = "~/.domain/favor/" + nmConf;
        WnObj oConf = Wn.checkObj(sys, phConf);

        // 准备接口
        WedisConfig conf = sys.io.readJson(oConf, WedisConfig.class);
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
            int limit = params.getInt("limit", 0);
            re = api.getAll(uid, limit);
        }
        // favor sum
        else if ("sum".equals(action)) {
            re = api.summary(uid);
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
            Collection<FavorIt> co = (Collection<FavorIt>) re;
            if (co.isEmpty()) {
                sys.out.println("(~nil~)");
            } else {
                int i = params.getInt("i", 1);
                String fmt = params.get("out", "%d) %s + %s");
                for (FavorIt fi : co) {
                    sys.out.printlnf(fmt, i++, fi.getTimeText(), fi.getTarget());
                }
            }
        }
        // 直接输出把
        else {
            sys.out.println(re.toString());
        }
    }

}
