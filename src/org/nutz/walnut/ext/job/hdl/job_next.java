package org.nutz.walnut.ext.job.hdl;

import java.util.Date;
import java.util.List;

import org.nutz.lang.Times;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJobService;

public class job_next implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnQuery query = new WnQuery();
        query.setv("pid", sys.io.check(null, WnJobService.root).id());
        query.limit(1);
        query.sortBy("job_ava", 1);
        query.setv("job_st", "wait");
        List<WnObj> list = sys.io.query(query);
        if (list == null || list.isEmpty())
            return;
        final WnObj jobDir = list.get(0);
        jobDir.setv("job_date", Times.sDT(new Date(jobDir.getLong("job_ava", 0))));
        sys.out.println(jobDir);
    }
}