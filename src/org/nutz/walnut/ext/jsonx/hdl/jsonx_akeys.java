package org.nutz.walnut.ext.jsonx.hdl;

import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class jsonx_akeys extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext ctx, ZParams params) {
        ctx.jfmt.setActived(params.val_check(0));
    }

}
