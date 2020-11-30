package org.nutz.walnut.ext.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_get extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        for (String id : params.vals) {
            WnObj o = sys.io.checkById(id);
            fc.add(o);
        }
    }

}
