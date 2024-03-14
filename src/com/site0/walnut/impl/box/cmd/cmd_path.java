package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_path extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0) {
            sys.out.println(sys.getCurrentObj().path());
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
