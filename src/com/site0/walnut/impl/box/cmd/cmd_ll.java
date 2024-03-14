package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_ll extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        int pos = sys.cmdOriginal.indexOf(' ');
        String re;
        if (pos > 0) {
            re = sys.exec2("ls -l " + sys.cmdOriginal.substring(pos));
        } else {
            re = sys.exec2("ls -l");
        }

        sys.out.print(re);
    }

}
