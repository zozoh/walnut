package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_env extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);

        // 得到会话的环境变量值表
        NutMap vars = sys.session.getVars();

        // 如果是移除
        if (params.has("u")) {
            String[] ss = Strings.splitIgnoreBlank(params.get("u"), "[, \t\n]");
            for (String varName : ss) {
                vars.remove(varName);
            }
            Wn.WC().security(new WnEvalLink(sys.io), () -> {
                sys.auth.saveSessionVars(sys.session);
            });
        }
        // 没有参数，列出所有环境变量
        else if (params.vals.length == 0) {
            for (String key : vars.keySet()) {
                sys.out.printlnf("%-8s : %s", key, vars.getString(key));
            }
        }
        // 一个值，仅仅列出值
        else if (params.vals.length == 1) {
            String str = params.vals[0];
            int pos = str.indexOf('=');
            // 设置变量
            if (pos > 0) {
                String key = str.substring(0, pos);
                String val = str.substring(pos + 1);
                String v2 = Wn.normalizeStr(val, sys);
                vars.put(key, v2);
                sys.auth.saveSessionVars(sys.session);
            }
            // 列出变量的值
            else {
                String key = str;
                String val = vars.getString(key);
                if (null != val)
                    sys.out.println(val);
            }
        }
        // 一个个的列出环境变量
        else {
            for (String key : params.vals) {
                String val = vars.getString(key);
                if (null != val)
                    sys.out.printlnf("%s : %s", key, val);
            }
        }

    }

}
