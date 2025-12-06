package com.site0.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_count extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "A");
        boolean showHidden = params.is("A");
        String tp = params.get("tp");

        String path;
        if (params.vals.length == 0) {
            path = sys.session.getEnv().getString("PWD");
        } else {
            path = params.vals[0];
        }

        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = Cmds.evalCandidateObjs(sys, new String[]{path}, list, Wn.Cmd.JOIN_CURRENT);
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
        if (params.has("match")) {
            String json = params.get("match", "{}");
            q.setAll(Wlang.map(json));
        }
        if (phObj.d0() != null)
            q.setv("d0", phObj.d0());
        if (phObj.d1() != null)
            q.setv("d1", phObj.d1());
        long childrenNum = sys.io.count(q);
        sys.out.print("" + childrenNum);
    }
}
