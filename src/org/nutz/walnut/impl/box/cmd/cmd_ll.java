package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_ll extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        int pos = sys.original.indexOf(' ');
        if (pos > 0) {
            sys.exec("ls -l " + sys.original.substring(pos));
        } else {
            sys.exec("ls -l");
        }
    }

}
