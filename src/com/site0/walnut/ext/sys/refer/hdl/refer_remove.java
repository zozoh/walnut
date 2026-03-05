package com.site0.walnut.ext.sys.refer.hdl;

import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.ext.sys.refer.ReferContext;
import com.site0.walnut.ext.sys.refer.ReferFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class refer_remove extends ReferFilter {

    @Override
    protected void process(WnSystem sys, ReferContext fc, ZParams params) {
        // 防守： 参数太少木有意义
        if (params.vals.length <= 1) {
            return;
        }
        // 分析参数
        String targetId = params.val_check(0);

        // 得到引用数
        int n = params.vals.length - 1;
        String[] referIds = new String[n];
        System.arraycopy(params.vals, 1, referIds, 0, n);

        // 记入引用计数
        WnReferApi refers = fc.getReferApi(sys);
        refers.remove(targetId, referIds);
    }

}
