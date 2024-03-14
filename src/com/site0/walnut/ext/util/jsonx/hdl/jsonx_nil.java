package com.site0.walnut.ext.util.jsonx.hdl;

import org.nutz.json.Json;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_nil extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj) {
            String json = params.val_check(0);
            fc.obj = Json.fromJson(json);
        }
    }

}
