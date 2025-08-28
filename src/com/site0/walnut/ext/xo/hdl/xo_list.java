package com.site0.walnut.ext.xo.hdl;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class xo_list extends XoFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "D");
    }

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        String prefix = params.val(0);
        boolean delimiter = !params.is("D");
        int limit = params.getInt("limit", 10);
        fc.result = fc.api.listObj(prefix, delimiter, limit);

    }

}
