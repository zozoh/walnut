package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_export extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 没参数显示所有环境变量
        if (args.length == 0) {
            for (String key : sys.se.envs().keySet()) {
                sys.out.printlnf("%-8s : %s", key, sys.se.envs().getString(key));
            }
        }
        // 逐个的添加
        else {
            NutMap envs = sys.se.envs();
            for (String s : args) {
                int pos = s.indexOf('=');
                if (pos >= 0) {
                    String key = Strings.trim(s.substring(0, pos));
                    String val = s.substring(pos + 1);
                    envs.setv(key, val);
                }
            }
            sys.sessionService.setEnvs(sys.se.id(), envs);
        }
    }
}
