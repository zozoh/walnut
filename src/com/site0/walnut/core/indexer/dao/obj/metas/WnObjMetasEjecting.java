package com.site0.walnut.core.indexer.dao.obj.metas;

import java.util.HashSet;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.eject.Ejecting;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;

public class WnObjMetasEjecting implements Ejecting {

    private HashSet<String> fieldNames;

    private JsonFormat jfmt;

    public WnObjMetasEjecting(HashSet<String> fieldNames) {
        this.fieldNames = fieldNames;
        this.jfmt = JsonFormat.compact().setQuoteName(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object eject(Object obj) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            NutMap vmap = new NutMap();

            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                if (!fieldNames.contains(key) && !"ph".equals(key)) {
                    Object val = en.getValue();
                    vmap.put(key, val);
                }
            }

            if (null != vmap && !vmap.isEmpty()) {
                return Json.toJson(vmap, jfmt);
            }
            return null;
        }
        throw Er.create("e.io.dao.entity.ejectMetas.NotSupportEjecting", obj.getClass().getName());
    }

}
