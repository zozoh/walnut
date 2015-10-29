package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_run extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 运行每个参数
        if (args.length > 0) {
            for (String arg : args) {
                sys.exec(arg);
            }
        }
        // 从管道里读取
        else if (sys.pipeId > 0) {
            String cmdLine;
            while (null != (cmdLine = sys.in.readLine())) {
                sys.exec(cmdLine);
            }
        }
    }

}
