package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_time extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0) {
            sys.out.println(getManual());
            return;
        }
        Stopwatch sw = Stopwatch.begin();
        sys.exec(Strings.join(" ", args));
        sw.stop();
        sys.out.println("\r\n"+sw);
    }

}
