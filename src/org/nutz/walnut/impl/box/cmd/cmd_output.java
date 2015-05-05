package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_output extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 看看是否需要延迟
        ZParams params = ZParams.parse(args, null);
        if (params.has("delay")) {
            long ms = params.getLong("delay");
            try {
                Thread.sleep(ms);
            }
            catch (InterruptedException e) {
                return;
            }
        }

        // 有内容
        if (params.vals.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String v : params.vals) {
                sb.append(Wn.normalizeStr(v, sys.se.envs())).append(' ');
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
