package com.site0.walnut.ext.sys.datex.hdl;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class datex_fmt extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String str = Ws.join(params.vals, " ");
        fc.fmt = Ws.sBlank(str, "yyyy-MM-dd HH:mm:ss");
    }

}
