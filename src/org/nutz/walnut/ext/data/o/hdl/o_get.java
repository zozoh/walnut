package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_get extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        for (String val : params.vals) {
            String[] ids = Ws.splitIgnoreBlank(val);
            for (String id : ids) {
                WnObj o = sys.io.checkById(id);
                fc.add(o);
            }
        }
    }

}
