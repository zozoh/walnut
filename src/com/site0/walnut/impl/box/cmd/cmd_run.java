package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;

public class cmd_run extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, null);
        String delay = params.getString("delay", null);
        String fph = params.getString("f", null);
        String cmdText;

        // 获取命令
        if (!Ws.isBlank(fph)) {
            WnObj oIn = Wn.checkObj(sys, fph);
            cmdText = sys.io.readText(oIn);
        }
        // 从管道里读取
        else {
            cmdText = sys.in.readAll();
        }

        // 直接执行
        if (!Ws.isBlank(cmdText)) {
            // 延迟执行
            if (!Ws.isBlank(delay)) {
                long delayInMs = Wtime.millisecond(delay);
                if (delayInMs > 0) {
                    // 延迟太长了，就不可以了
                    if (delayInMs > 3600000L) {
                        throw Er.create("e.run.delayToLong", delay);
                    }
                    Wlang.sleep(delayInMs);
                }
            }

            // 启动命令
            sys.exec(cmdText);
        }

    }

}
