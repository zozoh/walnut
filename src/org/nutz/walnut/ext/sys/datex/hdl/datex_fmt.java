package org.nutz.walnut.ext.sys.datex.hdl;

import org.nutz.walnut.ext.sys.datex.DatexContext;
import org.nutz.walnut.ext.sys.datex.DatexFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class datex_fmt extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String str = Ws.join(params.vals, " ");
        fc.fmt = Ws.sBlank(str, "yyyy-MM-dd HH:mm:ss");
    }

}
