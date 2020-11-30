package org.nutz.walnut.ext.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class o_fetch extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        for (String ph : params.vals) {
            WnObj o = Wn.checkObj(sys, ph);
            fc.add(o);
        }
    }

}
