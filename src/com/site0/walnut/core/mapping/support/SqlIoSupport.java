package com.site0.walnut.core.mapping.support;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Wtime;

public class SqlIoSupport {

    public static void tidyTimeField(NutBean bean, String key) {
        if (bean.has(key)) {
            long ams = bean.getLong(key);
            Date d = new Date(ams);
            String ds = Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss.SSS");
            bean.put(key, ds);
        }
    }

}
