package com.site0.walnut.ext.data.o.hdl;

import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class o_quiet extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        fc.quiet = true;
    }

}
