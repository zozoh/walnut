package org.nutz.walnut.ext.adv.app.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class AppInitApiProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        WnObj obj = ing.checkObj(WnRace.FILE);

        NutMap meta = ing.genMeta(false);
        if (ing.item.hasProperties()) {
            for (Map.Entry<String, Object> en : ing.item.getProperties().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                // 跨域
                if ("cross".equals(key)) {
                    if (val == Boolean.TRUE) {
                        meta.put("http-cross-origin", "*");
                    }
                }
                // 内容类型
                else if ("mime".equals(key)) {
                    meta.put("http-header-Content-Type", val);
                }
                // 特殊键：hook
                else if ("hook".equals(key)) {
                    if (Boolean.TRUE == val) {
                        meta.put("run-with-hook", true);
                    }
                }
                // 特殊键：dynamic
                else if ("dynamic".equals(key)) {
                    if (Boolean.TRUE == val) {
                        meta.put("http-dynamic-header", true);
                    }
                }
                // 其他的不支持
                else {
                    meta.put("http-header-Content-Type", val);
                }
            }
        }

        ing.io.appendMeta(obj, meta);
        ing.writeFile(obj);
    }

}
