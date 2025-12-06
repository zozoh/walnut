package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class jsonx_put extends JsonXFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(dft|path|raw)$");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 对于 Map
        if (fc.obj instanceof Map) {
            String key = params.val_check(0);
            // 记入新 Map
            if (params.vals.length == 1) {
                fc.obj = Wlang.map(key, fc.obj);
            }
            // 加入到当前 Map
            else {
                boolean isDft = params.is("dft");
                boolean asPath = params.is("path");
                boolean isRaw = params.is("raw");
                String json = params.val_check(1);
                if (":stdin".equalsIgnoreCase(json)) {
                    json = sys.in.readAll();
                }
                if (params.is("trim")) {
                    json = Ws.trim(json);
                }
                Object val = json;
                // 看看是不是对象
                if (!isRaw) {
                    try {
                        val = Json.fromJson(json);
                    }
                    catch (Throwable e) {}
                }
                NutMap map = NutMap.WRAP((Map<String, Object>) fc.obj);

                // 如果是路径模式，则尝试先定位一个子 Map
                if (asPath) {
                    String[] ss = Ws.splitIgnoreBlank(key, "[.]");
                    if (ss.length > 1) {
                        for (int i = 0; i < ss.length - 1; i++) {
                            String k = ss[i];
                            NutMap m2 = map.getAs(k, NutMap.class);
                            if (null == m2) {
                                m2 = new NutMap();
                                map.put(k, m2);
                            }
                            map = m2;
                        }
                        key = ss[ss.length - 1];
                    }
                }

                // 默认值
                if (isDft) {
                    map.putIfAbsent(key, val);
                }
                // 覆盖设置
                else {
                    map.put(key, val);
                }
            }
        }
        // 对于列表，
        else if (fc.obj instanceof Collection<?>
                 || fc.obj.getClass().isArray()) {
            String key = params.val_check(0);
            // 记入新 Map
            if (params.vals.length == 1) {
                fc.obj = Wlang.map(key, fc.obj);
            }
        }
    }

}
