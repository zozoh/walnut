package com.site0.walnut.ext.data.sqlx.hislog;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

public class HisRuntimeTrimData {

    String name;

    int maxSize;

    void doTrim(NutMap bean) {
        Object val = bean.get(name);
        if (null == val) {
            return;
        }
        if (val instanceof String) {
            String s = Ws.truncateByBytes(val.toString(), maxSize);
            if (!s.equals(val)) {
                bean.put(name, s);
            }
        }
    }

}
