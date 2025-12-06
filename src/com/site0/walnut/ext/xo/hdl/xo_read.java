package com.site0.walnut.ext.xo.hdl;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class xo_read extends XoFilter {

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        String key = params.val_check(0);
        fc.quiet = true;
        String re = fc.api.readText(key);
        sys.out.print(re);
    }

}
