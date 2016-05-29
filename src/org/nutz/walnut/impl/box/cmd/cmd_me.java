package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.meta.Pair;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
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

            // 设置到自身
            sys.me.setv(key, val);
            sys.usrService.set(sys.me, key, val);

            // 更新到当前 Session
            sys.exec("export '" + str + "'");
        }
        // 显示
        else {
            // 获取值
            WnUsr u = sys.usrService.check(sys.me.name());
            if (params.vals.length == 0) {
                for (String key : u.keySet()) {
                    String v = u.getString(key);
                    sys.out.printf("%8s : %s\n", key, v);
                }
            }
            // 只有一个值
            else if (params.vals.length == 1) {
                sys.out.println(u.getString(params.vals[0]));
            }
            // 指定的几个值
            else {
                for (String key : params.vals) {
                    String v = u.getString(key);
                    sys.out.printf("%8s : %s\n", key, v);
                }
            }
        }
    }

}
