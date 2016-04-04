package org.nutz.walnut.ext.job.hdl;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJob;

public class job_clean implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnQuery query = new WnQuery();
        query.setv("pid", sys.io.check(null, WnJob.root).id());
        query.setv("job_st", "done");
        sys.io.each(query, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                sys.exec("rm -r id:" + ele.id());
            }
        });
    }

}
