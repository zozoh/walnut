package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class jsonx_assign extends JsonXFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(dft)$");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        // 对于 Map
        if (fc.obj instanceof Map) {
            boolean isDft = params.is("dft");
            NutMap map = NutMap.WRAP((Map<String, Object>) fc.obj);

            for (String json : params.vals) {
                NutMap mm = Json.fromJson(NutMap.class, json);
                if (isDft) {
                    for (Map.Entry<String, Object> en : mm.entrySet()) {
                        String k = en.getKey();
                        Object v = en.getValue();
                        map.putDefault(k, v);
                    }
                }
                // 直接覆盖
                else {
                    map.putAll(mm);
                }
            }

            fc.obj = map;
        }
    }

}
