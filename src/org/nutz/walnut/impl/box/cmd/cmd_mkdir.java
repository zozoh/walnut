package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_mkdir extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "p");

        // 至少要有一个值
        if (params.vals.length == 0) {
            throw Er.create("e.cmd.mkdir.nopath");
        }

        // 得到目标路径
        for (String ph : params.vals) {
            String path = Wn.normalizeFullPath(ph, sys);
            // 如果是创建父
            if (params.has("p")) {
                sys.io.create(null, path, WnRace.DIR);
            }
            // 不能自动创建父
            else {
                String pph = Files.getParent(path);
                WnObj pobj = sys.io.check(null, pph);
                String nm = Files.getName(path);
                sys.io.create(pobj, nm, WnRace.DIR);
            }
        }
    }

}
