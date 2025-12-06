package com.site0.walnut.ext.xo.hdl;

import java.io.InputStream;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.xo.XoContext;
import com.site0.walnut.ext.xo.XoFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class xo_write extends XoFilter {

    @Override
    protected void process(WnSystem sys, XoContext fc, ZParams params) {
        String key = params.val_check(0);
        fc.quiet = true;
        NutMap meta = params.getAs("meta", NutMap.class);
        String text = params.val(1);
        if (!Ws.isBlank(text)) {
            fc.api.writeText(key, text, meta);
            return;
        }

        InputStream ins = sys.in.getInputStream();
        fc.api.write(key, ins, meta);

    }

}