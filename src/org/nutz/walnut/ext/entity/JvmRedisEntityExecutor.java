package org.nutz.walnut.ext.entity;

import java.util.Collection;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

public abstract class JvmRedisEntityExecutor extends JvmExecutor {

    protected WedisConfig prepareConfig(WnSystem sys, ZParams params, String typeName) {
        String nmConf = params.get("conf", "_" + typeName);
        if (!nmConf.endsWith(".json")) {
            nmConf += ".json";
        }
        String phConf = "~/.domain/" + typeName + "/" + nmConf;
        return Wedis.loadConfig(sys, phConf);
    }

    @SuppressWarnings("unchecked")
    protected <T> void output(WnSystem sys,
                              ZParams params,
                              Object re,
                              RedisEntityPrinter<T> printer) {
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
            Collection<T> co = (Collection<T>) re;
            if (co.isEmpty()) {
                sys.out.println("(~nil~)");
            } else {
                int skipI = params.getInt("skip", 0);
                int i = params.getInt("i", 1) + skipI;
                for (T it : co) {
                    printer.print(it, i++);
                }
            }
        }
        // 直接输出把
        else {
            sys.out.println(re.toString());
        }
    }

}
