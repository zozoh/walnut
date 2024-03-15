package com.site0.walnut.util;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;

public class HttpRegapi {

    public static NutMap QS(WnObj httpReq) {
        NutMap re = NutMap.NEW();
        for (String key : httpReq.keySet()) {
            if (key.startsWith("http-qs-")) {
                continue;
            }
            re.put(key, httpReq.get(key));
        }
        for (String key : httpReq.keySet()) {
            if (key.startsWith("http-qs-")) {
                re.put(key.substring("http-qs-".length()), httpReq.get(key));
            } else {}
        }
        return re;
    }
}
