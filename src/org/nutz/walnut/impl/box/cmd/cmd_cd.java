package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_cd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        String[] vals;
        if (args.length == 0) {
            vals = Lang.array("~");
        } else {
            vals = args;
        }

        // 得到要进入的目录
        String ph = Wn.normalizeFullPath(vals[0], sys.session);

        ph = Disks.getCanonicalPath(ph);

        // 检查这个目录是否存在
        WnObj o = sys.io.check(null, ph);

        if (!o.isDIR()) {
            throw Er.create("e.cmd.cd.nodir", ph);
        }

        // 确保可进入
        o = Wn.WC().whenEnter(o, false);

        // 修改会话中的设定
        sys.session.getVars().put("PWD", o.path());

    }

}
