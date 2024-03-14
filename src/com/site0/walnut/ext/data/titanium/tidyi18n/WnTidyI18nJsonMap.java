package com.site0.walnut.ext.data.titanium.tidyi18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnTidyI18nJsonMap {

    private WnIo io;

    private WnObj i18nFile;

    public WnTidyI18nJsonMap(WnIo io, WnObj f) {
        this.io = io;
        this.i18nFile = f;
    }

    private NutMap tidyMap(NutMap map) {
        // 排序
        List<String> keys = new ArrayList<>(map.size());
        keys.addAll(map.keySet());
        Collections.sort(keys);

        // 输出
        NutMap out = new NutMap();
        for (String key : keys) {
            Object val = map.get(key);
            // if (val instanceof Map) {
            // NutMap m2 = NutMap.WRAP((Map<String, Object>) val);
            // val = tidyMap(m2);
            // }
            out.put(key, val);
        }

        // 返回
        return out;
    }

    public String doTidy() {
        // 解析
        NutMap map = io.readJson(i18nFile, NutMap.class);

        // 排序
        NutMap out = tidyMap(map);

        // 返回
        JsonFormat jfmt = JsonFormat.nice();
        jfmt.setQuoteName(true);
        jfmt.setIndentBy("  ");
        return Json.toJson(out, jfmt);
    }

}
