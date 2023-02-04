package org.nutz.walnut.ext.util.strx.hdl;

import org.nutz.walnut.ext.util.strx.StrXContext;
import org.nutz.walnut.ext.util.strx.StrXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class strx_prefix extends StrXFilter {

    @Override
    protected void process(WnSystem sys, StrXContext fc, ZParams params) {
        String prefix = params.val(0);
        if (!Ws.isBlank(prefix) && !fc.data.startsWith(prefix)) {
            fc.data = prefix + fc.data;
        }
    }

}
