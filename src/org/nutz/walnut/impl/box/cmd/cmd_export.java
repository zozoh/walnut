package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;

public class cmd_export extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 没参数显示所有环境变量
        if (args.length == 0) {
            for (String key : sys.se.vars().keySet()) {
                sys.out.printlnf("%-8s : %s", key, sys.se.vars().getString(key));
            }
        }
        // 逐个的添加
        else {
            for (String s : args) {
                int pos = s.indexOf('=');
                if (pos >= 0) {
                    String key = Strings.trim(s.substring(0, pos));
                    String val = s.substring(pos + 1);
                    sys.se.var(key, val);
                    sys.se.persist(key);
                }
            }
            // 强制写入
            Wn.WC().security(new WnEvalLink(sys.io), () -> {
                sys.sessionService.save(sys.se);
            });
        }
    }
}
