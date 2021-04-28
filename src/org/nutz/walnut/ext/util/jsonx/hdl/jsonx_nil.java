package org.nutz.walnut.ext.util.jsonx.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class jsonx_nil extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj) {
            String json = params.val_check(0);
            fc.obj = Json.fromJson(json);
        }
    }

}
