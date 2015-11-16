package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_env extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);

        // 如果是移除
        if (params.has("u")) {
            String[] ss = Strings.splitIgnoreBlank(params.get("u"), "[, \t\n]");
            for (String varName : ss) {
                sys.se.var(varName, null);
            }
            sys.se.persist(ss);
            sys.sessionService.save(sys.se);
        }
        // 没有参数，列出所有环境变量
        else if (params.vals.length == 0) {
            for (String key : sys.se.vars().keySet()) {
                sys.out.printlnf("%-8s : %s", key, sys.se.vars().getString(key));
            }
        }
        // 一个值，仅仅列出值
        else if (params.vals.length == 1) {
            String key = params.vals[0];
            String val = sys.se.vars().getString(key);
            if (null != val)
                sys.out.println(val);
        }
        // 一个个的列出环境变量
        else {
            for (String key : params.vals) {
                String val = sys.se.vars().getString(key);
                if (null != val)
                    sys.out.printlnf("%s : %s", key, val);
            }
        }

    }

}
