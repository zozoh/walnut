package com.site0.walnut.ext.xo.hdl;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class xo_get extends XoFilter {

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        String key = params.val_check(0);
        fc.result = fc.api.getObj(key);
    }

}
