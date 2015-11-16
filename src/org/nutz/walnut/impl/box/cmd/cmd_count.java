package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_count extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "A");
        boolean showHidden = params.is("A");
        String tp = params.get("tp");

        String path;
        if (params.vals.length == 0) {
            path = sys.se.vars().getString("PWD");
        } else {
            path = params.vals[0];
        }

        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, new String[]{path}, list, true);
        String ph;
        if (list.isEmpty()) {
            if (args.length > 0) {
                throw Er.create("e.io.obj.noexists", args[0]);
            }
            ph = p.path();
        } else {
            ph = list.get(0).path();
        }

        // 计算路径下的文件数
        WnObj phObj = sys.io.fetch(null, ph);
        WnQuery q = Wn.Q.pid(phObj.id());
        if (!showHidden) {
            q.setv("nm", "^[^.].+$");
        }
        if (!Strings.isBlank(tp)) {
            q.setv("tp", tp);
        }
        long childrenNum = sys.io.count(q);
        sys.out.print("" + childrenNum);
    }
}
