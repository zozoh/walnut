package com.site0.walnut.ext.util.jsonx.hdl;

import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_akeys extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext ctx, ZParams params) {
        ctx.jfmt.setActived(params.val_check(0));
    }

}
