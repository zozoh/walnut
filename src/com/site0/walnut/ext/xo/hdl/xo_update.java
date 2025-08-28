package com.site0.walnut.ext.xo.hdl;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class xo_update extends XoFilter {

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        String key = params.val_check(0);
        String str = params.val(1);
        if (Ws.isBlank(str)) {
            str = sys.in.readAll();
        }
        NutMap delta = Wlang.map(str);
        if (null == delta || delta.isEmpty()) {
            return;
        }
        fc.quiet = true;
        fc.api.appendMeta(key, delta);
    }

}
