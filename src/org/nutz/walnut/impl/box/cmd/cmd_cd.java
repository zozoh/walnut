package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_cd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        if (args.length == 0) {
            args = Lang.array("~");
        }

        // 计算要列出的目录并得到当前目录
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, args, list, true);

        String ph;
        if (list.isEmpty()) {
            ph = p.path();
        } else {
            ph = list.get(0).path();
        }

        sys.sessionService.setEnv(sys.se, "PWD", ph);
    }

}
