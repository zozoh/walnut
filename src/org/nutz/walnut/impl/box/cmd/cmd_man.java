package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_man extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 没参数，则用自己的说明
        if (args.length == 0) {
            sys.out.println(this.getManual());
        }
        // 否则用第一个参数作为要查看说明的命令
        else {
            JvmExecutor cmd = sys.jef.get(args[0]);
            if (null == cmd) {
                sys.err.printlnf("e.cmd.notfound : %s", args[0]);
            } else {
                sys.out.println(cmd.getManual());
            }
        }
    }

}
