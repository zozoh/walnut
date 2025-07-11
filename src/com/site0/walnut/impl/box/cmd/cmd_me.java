package com.site0.walnut.impl.box.cmd;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_me extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        WnUser me = sys.getMe();

        // 设置变量
        if (params.has("set")) {
            String str = params.check("set");
            Pair<String> v = Pair.create(str);
            String key = v.getName();
            String val = v.getValue();

            String v2 = null != val ? Wn.normalizeStr(val, sys) : null;

            // 设置到自身
            me.setMeta(key, v2);

            // 更新到当前 Session
            sys.session.loadEnvFromUser(me);

            // 持久化
            __do_save(sys, me);
        }
        // 删除变量
        else if (params.has("unset")) {
            String keys = params.get("unset");
            String[] keyList = Strings.splitIgnoreBlank(keys, "[, \t\n]");

            // 删除元数据
            me.removeMeta(keyList);

            // 更新到当前 Session
            sys.session.loadEnvFromUser(me);

            // 持久化
            __do_save(sys, me);
        }
        // 读取变量
        else if (params.has("get")) {
            NutMap jsonRe = NutMap.NEW();
            me.mergeToBean(jsonRe);
            String key = params.get("get");
            Object val;
            if (key.matches("^(passwd|salt|oauth_.+|wx_.+)$")) {
                val = "******";
            } else {
                val = jsonRe.get(key);
            }
            sys.out.println(val);
        }
        // 显示
        else {
            NutMap jsonRe = NutMap.NEW();
            me.mergeToBean(jsonRe);

            // 去掉敏感信息
            jsonRe.remove("passwd");
            jsonRe.remove("salt");
            jsonRe.pickAndRemoveBy("^(passwd|salt|oauth_.+|wx_.+)$");

            // JSON 输出
            if (params.is("json")) {
                sys.out.println(Json.toJson(jsonRe));
            }
            // 格式化输出
            else {
                for (String key : jsonRe.keySet()) {
                    Object v = jsonRe.get(key);
                    String s = Castors.me().castToString(v);
                    sys.out.printf("%8s : %s\n", key, s);
                }
            }
        }
    }

    private void __do_save(WnSystem sys, WnUser me) {
        sys.nosecurity(() -> {
            sys.auth.saveUserMeta(me);
            sys.auth.saveSessionEnv(sys.session);
        });
    }

}
