package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_mount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, null);

        if (params.vals.length != 2) {
            throw Er.create("e.cmd.invalidargs", args);
        }

        String mnt = params.vals[0];
        String val = params.vals[1];

        // 目标必须是一个目录
        String ph = Wn.normalizeFullPath(val, sys.se);
        WnObj o = sys.io.createIfNoExists(null, ph, WnRace.DIR);

        // 设置挂载点
        sys.io.setMount(o, mnt);
    }

}
