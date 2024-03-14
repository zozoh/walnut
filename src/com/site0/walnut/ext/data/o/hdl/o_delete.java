package com.site0.walnut.ext.data.o.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
