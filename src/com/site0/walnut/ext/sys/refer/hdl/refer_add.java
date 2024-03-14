package com.site0.walnut.ext.sys.refer.hdl;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class refer_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 防守： 参数太少木有意义
        if (hc.params.vals.length <= 1) {
            return;
        }
        // 分析参数
        String targetId = hc.params.val_check(0);

        // 得到引用数
        int n = hc.params.vals.length - 1;
        String[] referIds = new String[n];
        System.arraycopy(hc.params.vals, 1, referIds, 0, n);

        // 记入引用计数
        WnReferApi refers = sys.services.getReferApi();
        refers.add(targetId, referIds);
    }

}
