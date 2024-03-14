package com.site0.walnut.ext.sys.refer.hdl;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class refer_count implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String targetId = hc.params.val_check(0);
        WnReferApi refers = sys.services.getReferApi();
        long n = refers.count(targetId);

        sys.out.println(n);
    }

}
