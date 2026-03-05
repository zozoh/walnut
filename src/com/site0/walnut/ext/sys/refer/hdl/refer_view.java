package com.site0.walnut.ext.sys.refer.hdl;

import java.util.Set;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.ext.sys.refer.ReferFilter;
import com.site0.walnut.ext.sys.refer.ReferContext;

public class refer_view extends ReferFilter {

    @Override
    protected void process(WnSystem sys, ReferContext fc, ZParams params) {
        String targetId = params.val_check(0);
        
        // 仅仅是查看的话，就无需验证权限了
        WnReferApi refers;
        if (null != fc.oDir) {
            refers = fc.getReferApi(sys);
        } else {
            refers = sys.services.getReferApi();
        }

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
