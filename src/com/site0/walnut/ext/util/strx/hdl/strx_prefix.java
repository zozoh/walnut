package com.site0.walnut.ext.util.strx.hdl;

import com.site0.walnut.ext.util.strx.StrXContext;
import com.site0.walnut.ext.util.strx.StrXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class strx_prefix extends StrXFilter {

    @Override
    protected void process(WnSystem sys, StrXContext fc, ZParams params) {
        String prefix = params.val(0);
        if (!Ws.isBlank(prefix) && !fc.data.startsWith(prefix)) {
            fc.data = prefix + fc.data;
        }
    }

}
