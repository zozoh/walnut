package org.nutz.walnut.ext.job.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnRun;
import org.nutz.web.Webs.Err;

public class job_show extends job_abstract {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (hc.args.length == 0) {
            throw Err.create("e.cmds.need_args");
        }
        WnRun.sudo(sys, () -> {
            WnObj jobRoot = sys.io.check(null, WnJobService.root);
            for (String id : hc.args) {
                WnObj jobDir = sys.io.check(jobRoot, id);
                if (jobDir == null) {
                    sys.err.println("no such job. id=" + id);
                    continue;
                }
                // 只有root可以看其他人的job
                if (!sys.getMe().isRoot()) {
                    if (!sys.getMyName().equals(jobDir.get("job_create_user"))) {
                        sys.err.println("permission denied for job id=" + id);
                        continue;
                    }
                }
                Json.toJson(sys.out.getWriter(), jobDir);
                WnObj cmd = sys.io.fetch(jobRoot, "cmd");
                if (cmd == null)
                    continue;
                sys.out.println(sys.io.readText(cmd));
                sys.out.println("//--------------------//");
            }
        });
    }

}
