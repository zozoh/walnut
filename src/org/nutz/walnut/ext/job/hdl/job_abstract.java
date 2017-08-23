package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJob;

public abstract class job_abstract implements JvmHdl {

    public static WnQuery query(WnSystem sys, String user) {
        WnQuery query = new WnQuery();
        query.setv("pid", jobRootDir(sys).id());
        query.sortBy("job_ava", 1);
        query.setv("job_st", "wait");
        if (user != null)
            query.setv("job_create_user", user);
        return query;
    }
    
    public static WnObj jobRootDir(WnSystem sys) {
        return sys.io.check(null, WnJob.root);
    }
    
    public static String user(String me, String param) {
        if ("root".equals(me) && param != null)
            return param;
        return me;
    }
}
