package org.nutz.walnut.ext.jsonx.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class jsonx_put extends JsonXFilter {

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
            String key = params.val_check(0);
            String json = params.val_check(1);
            Object val = Json.fromJson(json);
            Map<String, Object> map = (Map<String, Object>) fc.obj;
            if (params.is("dft")) {
                map.putIfAbsent(key, val);
            } else {
                map.put(key, val);
            }
        }
    }

}
