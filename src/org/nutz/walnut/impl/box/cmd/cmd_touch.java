package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_touch extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "iocnqhbslVNPH");
        if (params.vals.length == 0) {
            throw Err.create("e.touch.cmd.no_args");
        }

        WnObj p = sys.getCurrentObj();

        List<WnObj> list = new LinkedList<WnObj>();

        for (int i = 0; i < params.vals.length; i++) {
            String path = Wn.normalizePath(params.vals[i], sys);
            WnObj obj = sys.io.fetch(p, path);
            // 没有就创建
            if (obj == null) {
                obj = sys.io.create(p, path, WnRace.FILE);
            }
            // 否则更新最后修改时间
            else {
                obj.lastModified(Wn.now());
                sys.io.appendMeta(obj, "^lm$");
            }
            list.add(obj);
        }

        if (params.is("o")) {
            Cmds.output_objs(sys, params, null, list, true);
        }
    }

}
