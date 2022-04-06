package org.nutz.walnut.ext.sys.refer.hdl;

import org.nutz.walnut.core.WnReferApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class refer_count implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String targetId = hc.params.val_check(0);
        WnReferApi refers = sys.services.getReferApi();
        long n = refers.count(targetId);

        sys.out.println(n);
    }

}
