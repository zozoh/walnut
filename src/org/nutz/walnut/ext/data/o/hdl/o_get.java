package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
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
