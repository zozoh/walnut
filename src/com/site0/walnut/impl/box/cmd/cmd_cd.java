package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.util.Disks;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class cmd_cd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        String[] vals;
        if (args.length == 0) {
            vals = Wlang.array("~");
        } else {
            vals = args;
        }

        // 得到要进入的目录
        String ph = Wn.normalizeFullPath(vals[0], sys.session);

        ph = Disks.getCanonicalPath(ph);

        // 确保以目录结尾
        if (!ph.endsWith("/"))
            ph += "/";

        // 检查这个目录是否存在
        WnObj o = sys.io.check(null, ph);

        if (!o.isDIR()) {
            throw Er.create("e.cmd.cd.nodir", ph);
        }

        // 确保可进入
        o = Wn.WC().whenEnter(o, false);

        // 修改会话中的设定
        sys.session.updateEnv("PWD", o.getRegularPath());
        sys.auth.saveSessionEnv(sys.session);
    }

}
