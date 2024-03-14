package com.site0.walnut.ext.sys.refer.hdl;

import java.util.Set;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class refer_view implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String targetId = hc.params.val_check(0);
        WnReferApi refers = sys.services.getReferApi();
        Set<String> referIds = refers.all(targetId);

        if (referIds.isEmpty()) {
            sys.out.println("~ empty ~");
        } else {
            int i = 0;
            for (String rid : referIds) {
                sys.out.printlnf("%d) %s", i++, rid);
            }
        }
    }

}
