package org.nutz.walnut.impl.box.cmd;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

@IocBean
public class cmd_export extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 没参数显示所有环境变量
        if (args.length == 0) {
            for (String key : sys.se.envs().keySet()) {
                sys.out.writeLinef("%-8s : %s", key, sys.se.envs().getString(key));
            }
        }
        // 逐个的添加
        else {
            NutMap envs = sys.se.envs();
            for (String s : args) {
                Pair<String> attr = Pair.create(s);
                envs.setv(attr.getName(), attr.getValue());
            }
            sys.sessionService.setEnvs(sys.se.id(), envs);
        }
    }

}
