package org.nutz.walnut.ext.old.job.hdl;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.old.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class job_clean extends job_abstract {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnQuery query = new WnQuery();
        query.setv("pid", sys.io.check(null, WnJobService.root).id());
        query.setv("job_st", "done");
        sys.io.each(query, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                sys.exec("rm -r id:" + ele.id());
            }
        });
    }

}
