package org.nutz.walnut.ext.jsonx.hdl;

import org.nutz.mapl.Mapl;
import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class jsonx_get extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        String keyPath = params.val_check(0);
        Object val = Mapl.cell(fc.obj, keyPath);
        fc.obj = val;
    }

}
