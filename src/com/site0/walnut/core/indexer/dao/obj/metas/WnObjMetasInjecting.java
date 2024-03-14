package com.site0.walnut.core.indexer.dao.obj.metas;

import java.util.HashSet;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.inject.Injecting;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.err.Er;

/**
 * 数据库读出的一段 JSON 数据，这个数据应该被解析，并合并到对象中
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnObjMetasInjecting implements Injecting {

    private static final Log log = Wlog.getIO();

    private HashSet<String> fieldNames;

    public WnObjMetasInjecting(HashSet<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object obj, Object value) {
        if (null == value) {
            return;
        }
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            NutMap vmap = null;
            // 如果给入的值就是 Map
            if (value instanceof Map<?, ?>) {
                vmap = NutMap.WRAP((Map<String, Object>) value);
            }
            // 那么是字符串的话，试图解析一下
            else if (value instanceof CharSequence) {
                try {
                    vmap = Json.fromJson(NutMap.class, value.toString());
                }
                catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Invalid JSON meta format: " + value, e);
                    }
                }
            }
            // 移除表内字段
            if (null != vmap && !vmap.isEmpty()) {
                // 一定是不要路径的
                vmap.remove("ph");
                if (!fieldNames.isEmpty()) {
                    for (String key : fieldNames) {
                        vmap.remove(key);
                    }
                }
                map.putAll(vmap);
            }
            return;
        }
        throw Er.create("e.io.dao.entity.injectMetas.NotSupportInjecting",
                        obj.getClass().getName());
    }

}
