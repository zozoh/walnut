package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.util.Ws;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_trim extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (sys.pipeId > 0) {
            ZParams params = ZParams.parse(args, "lr");

            String str = sys.in.readAll();

            // 左侧
            if (params.is("l")) {
                sys.out.print(Ws.trimLeft(str));
            }
            // 右侧
            else if (params.is("r")) {
                sys.out.print(Ws.trimRight(str));
            }
            // 全部
            else {
                sys.out.print(Ws.trim(str));
            }
        }
    }

}
