package org.nutz.walnut.ext.old.fake;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_fake_prerun extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        String stdIn = null;

        if (sys.pipeId > 0)
            stdIn = sys.in.readAll();

        for (String arg : args) {
            sys.out.println(Strings.dup('-', 40));
            sys.out.println("CMD:> " + arg);
            sys.out.println(Strings.dup('-', 40));
            String re = sys.exec2(arg, stdIn);
            sys.out.println(re);
        }
    }

}
