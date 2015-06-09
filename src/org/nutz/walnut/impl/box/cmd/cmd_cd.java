package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_cd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        String[] vals;
        if (args.length == 0) {
            vals = Lang.array("~");
        } else {
            vals = args;
        }

        // 计算要列出的目录并得到当前目录
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, vals, list, true);

        String ph;
        if (list.isEmpty()) {
            if (args.length > 0) {
                throw Er.create("e.io.obj.noexists", args[0]);
            }
            ph = p.path();
        } else {
            ph = list.get(0).path();
        }

        // 修改会话中的设定
        sys.se = sys.sessionService.setEnv(sys.se.id(), "PWD", ph);

    }

}
