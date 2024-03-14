package com.site0.walnut.impl.box.cmd;

import java.util.Arrays;

import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_time extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0) {
            sys.out.println(getManual());
            return;
        }
        // 需要执行多少次呢?
        int count = 1;
        if ("-t".equals(args[0])) {
            count = Integer.parseInt(args[1]);
            args = Arrays.copyOfRange(args, 2, args.length);
        }
        for (int i = 0; i < count; i++) {
            Stopwatch sw = Stopwatch.begin();
            sys.exec(Strings.join(" ", args));
            sw.stop();
            sys.out.println("\r\n"+sw);
        }
    }

}
