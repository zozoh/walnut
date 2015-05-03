package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_echo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 有内容
        if (args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(Wn.normalizeStr(arg, sys.se.envs())).append(' ');
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);

            sys.out.writeLine(sb.toString());
        }
        // 没内容，写空
        else {
            sys.out.writeLine("");
        }
    }

}
