package com.site0.walnut.ext.util.jsonx.hdl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class jsonx_set extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain)$");
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj) {
            fc.obj = new NutMap();
        }

        boolean needExplain = params.is("explain");

        NutMap delta = new NutMap();
        for (String str : params.vals) {
            NutMap o = Json.fromJson(NutMap.class, str);
            delta.putAll(o);
        }

        // 对于 Map
        if (fc.obj instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) fc.obj);
            if (needExplain) {
                Wn.explainMetaMacroInPlaceDeeply(delta);
                NutMap d2 = (NutMap) Wn.explainObj(map, delta);
                delta = d2;
            }
            map.putAll(delta);
        }
        // 对于或者集合
        else if (fc.obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) fc.obj;

            for (Object ele : col) {
                if (ele instanceof Map) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) ele);
                    if (needExplain) {
                        NutMap d2 = delta.duplicate();
                        Wn.explainMetaMacroInPlaceDeeply(d2);
                        NutMap d3 = (NutMap) Wn.explainObj(map, d2);
                        delta = d3;
                    }
                    map.putAll(delta);
                }
            }
        }
        // 对于数组
        else if (fc.obj.getClass().isArray()) {
            int n = Array.getLength(fc.obj);
            for (int i = 0; i < n; i++) {
                Object ele = Array.get(fc.obj, i);
                if (ele instanceof Map) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) ele);
                    if (needExplain) {
                        NutMap d2 = delta.duplicate();
                        Wn.explainMetaMacroInPlaceDeeply(d2);
                        NutMap d3 = (NutMap) Wn.explainObj(map, d2);
                        delta = d3;
                    }
                    map.putAll(delta);
                }
            }
        }

    }

}
