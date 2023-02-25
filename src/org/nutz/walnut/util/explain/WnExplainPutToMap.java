package org.nutz.walnut.util.explain;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

public class WnExplainPutToMap {

    private boolean is_put_all;

    private String key;

    private WnExplain explain;

    public WnExplainPutToMap(String key, WnExplain explain) {
        this.key = key;
        this.explain = explain;
        if ("...".equals(key)) {
            is_put_all = true;
        }
    }

    @SuppressWarnings("unchecked")
    public void putTo(NutBean context, Map<String, Object> map) {
        Object val = explain.explain(context);
        if (is_put_all) {
            if (null != val && val instanceof Map<?, ?>) {
                NutMap vmap = NutMap.WRAP((Map<String, Object>) val);
                map.putAll(vmap);
            }
            // 不能解构，那么抛一个错误
            else {
                throw Er.create("e.explain.decon.NoMap", key);
            }
        } else {
            map.put(key, val);
        }

    }
}
