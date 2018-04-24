package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

public class job_delete extends job_abstract{

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (hc.args.length == 0) {
            throw Err.create("e.cmds.need_args");
        }
        sys.nosecurity(() -> {
            for (String id : hc.args) {
                WnObj jobDir = sys.io.get(id);
                if (jobDir == null)
                    continue;
                if (!Wn.WC().isAdminOf(sys.usrService, "root", "job")) {
                    if (!jobDir.getString("job_create_user").equals(sys.me.name())) {
                        sys.err.println("not your job id="+id);
                        continue;
                    }
                }
                sys.io.delete(jobDir, true);
                WnQuery query = new WnQuery().setv("pid", jobRootDir(sys).id());
                query.setv("job_pid", id);
                for (WnObj ele : sys.io.query(query)) {
                    sys.io.delete(ele, true);
                }
            }
        });
    } 
}
