package org.nutz.walnut.ext.o.hdl;

import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_quiet extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        fc.alreadyOutputed = true;
    }

}
