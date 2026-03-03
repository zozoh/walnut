package com.site0.walnut.ext.xo.hdl;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class xo_reset extends XoFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(quiet)$");
    }

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        fc.quiet = true;
        int re = fc.api.resetClients();
        if (!params.is("quiet")) {
            sys.out.printlnf("closed %d clients instance", re);
        }
    }

}