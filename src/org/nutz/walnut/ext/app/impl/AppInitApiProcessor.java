package org.nutz.walnut.ext.app.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
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
                // 其他的不支持
                else {
                    throw Er.create("e.cmd.app_init.invalid_api_prop", key);
                }
            }
        }

        ing.io.appendMeta(obj, meta);
        ing.writeFile(obj);
    }

}
