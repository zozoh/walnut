package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_merge extends JsonXFilter {

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        // 对于 Map
        if (fc.obj instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) fc.obj);

            for (String json : params.vals) {
                NutMap mm = Json.fromJson(NutMap.class, json);
                map.mergeWith(mm);
            }

            fc.obj = map;
        }

    }

}
