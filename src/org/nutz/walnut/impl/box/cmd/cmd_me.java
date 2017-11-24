package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_me extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        // 设置变量
        if (params.has("set")) {
            String str = params.check("set");
            Pair<String> v = Pair.create(str);
            String key = v.getName();
            String val = v.getValue();
            String v2 = Wn.normalizeStr(val, sys);

            // 设置到自身
            sys.me.setv(key, v2);
            sys.usrService.set(sys.me, key, v2);

            // 更新到当前 Session
            sys.exec("export '" + str + "'");
        }
        // 删除变量
        else if (params.has("unset")) {
            String keys = params.get("unset");
            String[] keyList = Strings.splitIgnoreBlank(keys, "[, \t\n]");

            // 循环删除
            for (String key : keyList) {
                sys.usrService.set(sys.me, key, null);
                sys.me.remove(key);
                sys.se.var(key, null);
            }

            // 更新 session
            Wn.WC().security(new WnEvalLink(sys.io), () -> {
                sys.se.save();
            });
        }
        // 显示
        else {
            NutMap jsonRe = NutMap.NEW();
            // 获取值
            WnUsr u = sys.usrService.check(sys.me.name());
            if (params.vals.length == 0) {
                for (String key : u.keySet()) {
                    String v = u.getString(key);
                 // 一定要过滤的字段
                    if (key.matches("^(salt|passwd)$")) {
                        continue;
                    }
                    if (params.is("json")) {
                        jsonRe.setv(key, v);
                    } else {
                        if (null != v) {
                            sys.out.printf("%8s : %s\n", key, v);
                        }
                    }
                }
                if (params.is("json")) {
                    sys.out.println(Json.toJson(jsonRe));
                }
            }
            // 只有一个值
            else if (params.vals.length == 1) {
                if (params.is("json")) {
                    jsonRe.setv(params.vals[0], u.getString(params.vals[0]));
                    sys.out.println(Json.toJson(jsonRe));
                } else {
                    sys.out.println(u.getString(params.vals[0]));
                }
            }
            // 指定的几个值
            else {
                if (params.is("json")) {
                    for (String key : params.vals) {
                        // 一定要过滤的字段
                        if (key.matches("^(salt|passwd)$"))
                            continue;
                        jsonRe.setv(key, u.get(key));
                    }
                    sys.out.println(Json.toJson(jsonRe));
                } else {
                    for (String key : params.vals) {
                        String v = u.getString(key);
                        sys.out.printf("%8s : %s\n", key, v);
                    }
                }

            }
        }
    }

}
