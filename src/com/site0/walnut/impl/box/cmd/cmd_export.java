package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.util.Wn;

public class cmd_export extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        NutMap vars = sys.session.getVars();
        // 没参数显示所有环境变量
        if (args.length == 0) {
            for (String key : vars.keySet()) {
                sys.out.printlnf("%-8s : %s", key, vars.getString(key));
            }
        }
        // 逐个的添加
        else {
            for (String s : args) {
                int pos = s.indexOf('=');
                if (pos >= 0) {
                    String key = Strings.trim(s.substring(0, pos));
                    String val = s.substring(pos + 1);
                    String v2 = Wn.normalizeStr(val, sys);
                    vars.put(key, v2);
                }
            }
            // 强制写入
            Wn.WC().security(new WnEvalLink(sys.io), () -> {
                sys.auth.saveSessionVars(sys.session);
            });
        }
    }
}
