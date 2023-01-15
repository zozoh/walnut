package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;

public class cmd_run extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 预处理
        String cmdTmpl = null;
        String varJson = null;

        // 从管道里读取
        if (args.length == 0) {
            cmdTmpl = sys.in.readAll();
        }
        // 拼合参数
        else {
            // 准备参数模板
            String[] cmds = new String[args.length];
            int i = 0;
            for (String arg : args) {
                // Escape
                String val = arg.replaceAll("([\"'])", "\\\\$1");
                cmds[i++] = val;
            }
            // cmdTmpl = Lang.concatBy(" '%s' ", cmds).toString();
            cmdTmpl = Ws.join(cmds, " ");

            // 读取变量集
            varJson = sys.in.readAll();
        }

        // 直接执行
        if (Strings.isBlank(varJson)) {
            sys.exec(cmdTmpl);
        }
        // 执行模板
        else {
            NutMap vars = Lang.map(varJson);
            String cmdText = WnTmpl.exec(cmdTmpl, vars);
            sys.exec(cmdText);
        }
    }

}
