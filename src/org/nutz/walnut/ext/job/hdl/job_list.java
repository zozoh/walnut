package org.nutz.walnut.ext.job.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.ZParams;

public class job_list extends job_abstract {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnRun.sudo(sys, () -> {

                ZParams params = ZParams.parse(hc.args, null);
                //String mode = params.get("mode", "simple");
                String user = user(sys.me.name(), params.get("user"));
                
                WnQuery query = new WnQuery();
                query.setv("pid", jobRootDir(sys).id());
                query.sortBy("job_ava", 1);
                query.setv("job_st", "wait");
                
                query.limit(params.getInt("limit", 20));
                query.setv("job_create_user", user);
                query.exists("job_pid", false);
                
                List<WnObj> list = sys.io.query(query);
                if (list == null || list.isEmpty())
                    return;
                Json.toJson(sys.out.getWriter(), list);
        });
    }

}
