package org.nutz.walnut.impl.box.cmd;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

@IocBean
public class cmd_mkdir extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "p");

        // 至少要有一个值
        if (params.vals.length == 0) {
            throw Er.create("e.cmd.mkdir.nopath");
        }

        // 得到当前路径
        WnObj p = this.getCurrentObj(sys);

        // 得到目标路径
        for (String path : params.vals) {
            // 如果是创建父
            if (params.has("p")) {
                sys.io.create(p, path, WnRace.DIR);
            }
            // 不能自动创建父
            else {
                String pph = Files.getParent(path);
                WnObj pobj = sys.io.check(p, pph);
                String nm = Files.getName(path);
                sys.io.create(pobj, nm, WnRace.DIR);
            }
        }
    }

}
