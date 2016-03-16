package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

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

        ZParams params = ZParams.parse(args, "pocnql");

        // 至少要有一个值
        if (params.vals.length == 0) {
            throw Er.create("e.cmd.mkdir.nopath");
        }

        // 准备输出
        List<WnObj> outs = new LinkedList<WnObj>();

        // 得到目标路径
        for (String ph : params.vals) {
            String path = Wn.normalizeFullPath(ph, sys);
            WnObj o;
            // 如果是创建父
            if (params.has("p")) {
                o = sys.io.create(null, path, WnRace.DIR);
            }
            // 不能自动创建父
            else {
                String pph = Files.getParent(path);
                WnObj pobj = sys.io.check(null, pph);
                String nm = Files.getName(path);
                o = sys.io.create(pobj, nm, WnRace.DIR);
            }
            // 计入结果
            outs.add(o);
        }

        // 输出
        if (params.is("o")) {
            this.output_objs(sys, params, null, outs);
        }
    }

}
