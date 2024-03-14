package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_set extends JsonXFilter {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj) {
            fc.obj = new NutMap();
        }

        // 对于 Map
        if (fc.obj instanceof Map) {
            Map map = (Map) fc.obj;

            for (String str : params.vals) {
                NutMap o = Json.fromJson(NutMap.class, str);
                map.putAll(o);
            }
        }

    }

}
