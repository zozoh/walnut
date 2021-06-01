package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_delete extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "r");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        boolean r = params.is("r");
        for (WnObj o : fc.list) {
            sys.io.delete(o, r);
        }
    }

}
