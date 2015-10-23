package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_path extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0) {
            sys.out.println(this.getCurrentObj(sys).path());
            return;
        }

        if (args.length == 1) {
            sys.out.println(Wn.checkObj(sys.io, args[0]).path());
            return;
        }

        for (String arg : args) {
            WnObj o = Wn.getObj(sys.io, arg);
            sys.out.printlnf("%s : %s", arg, o.path());
        }
    }

}
